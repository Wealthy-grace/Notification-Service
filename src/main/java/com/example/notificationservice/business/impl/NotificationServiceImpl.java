package com.example.notificationservice.business.impl;

import com.example.notificationservice.business.client.PropertyServiceClient;
import com.example.notificationservice.business.client.UserServiceClient;
import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.business.interfaces.NotificationService;
import com.example.notificationservice.business.interfaces.NotificationStatistics;
import com.example.notificationservice.business.mapper.NotificationMapper;
import com.example.notificationservice.domain.dto.NotificationDto;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.domain.dto.UserDto;
import com.example.notificationservice.domain.request.NotifyNewPropertyRequest;
import com.example.notificationservice.domain.request.SendNotificationRequest;
import com.example.notificationservice.domain.request.UpdatePropertyRentedRequest;
import com.example.notificationservice.domain.response.PropertyServiceResponse;
import com.example.notificationservice.exceptions.ResourceNotFoundException;

import com.example.notificationservice.persistence.model.Notification;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;
import com.example.notificationservice.persistence.repository.NotificationRepository;
import com.example.notificationservice.producer.NotificationProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Notification Service Implementation with RabbitMQ Integration
 * Main business logic for notification management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final PropertyServiceClient propertyServiceClient;
    private final UserServiceClient userServiceClient;
    private final NotificationProducer notificationProducer; // RabbitMQ Producer

    // ========== CIRCUIT BREAKER PROTECTED METHODS ==========

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService")
    public UserDto getUserWithCircuitBreaker(Long userId) {
        log.debug("🔍 Calling User Service for user ID: {}", userId);
        return userServiceClient.getUserById(userId);
    }

    private UserDto getUserByIdFallback(Long userId, Exception ex) {
        log.error("⚠️ USER SERVICE CIRCUIT BREAKER ACTIVATED for user ID: {}. Reason: {}",
                userId, ex.getMessage());

        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setEmail("fallback-user-" + userId + "@system.local");
        fallbackUser.setFirstName("System");
        fallbackUser.setLastName("User");
        fallbackUser.setUsername("fallback-user-" + userId);

        log.warn("📧 Using FALLBACK user data for notification to user ID: {}", userId);
        return fallbackUser;
    }

    @CircuitBreaker(name = "propertyService", fallbackMethod = "getPropertyByIdFallback")
    @Retry(name = "propertyService")
    public PropertyServiceResponse getPropertyWithCircuitBreaker(Long propertyId) {
        log.debug("🔍 Calling Property Service for property ID: {}", propertyId);
        return propertyServiceClient.getPropertyById(propertyId);
    }

    private PropertyServiceResponse getPropertyByIdFallback(Long propertyId, Exception ex) {
        log.error("⚠️ PROPERTY SERVICE CIRCUIT BREAKER ACTIVATED for property ID: {}. Reason: {}",
                propertyId, ex.getMessage());

        PropertyServiceResponse fallbackProperty = new PropertyServiceResponse();
        fallbackProperty.setPropertyId(propertyId);
        fallbackProperty.setTitle("Property Information Temporarily Unavailable");
        fallbackProperty.setDescription("We're experiencing technical difficulties. Please check back later.");
        fallbackProperty.setAddress("N/A");
        fallbackProperty.setRentAmount(BigDecimal.valueOf(0.0));

        log.warn("🏠 Using FALLBACK property data for property ID: {}", propertyId);
        return fallbackProperty;
    }

    // ========== NOTIFICATION METHODS WITH RABBITMQ ==========

    @Override
    @Transactional
    public NotificationDto sendNotification(SendNotificationRequest request) {
        log.info("📨 Creating notification for user: {} ({})", request.getUserName(), request.getUserEmail());

        try {
            // Create notification entity
            Notification notification = notificationMapper.fromRequest(request);
            notification.setStatus(NotificationStatus.PENDING);

            // Save to database
            notification = notificationRepository.save(notification);
            log.info("💾 Notification saved with ID: {}", notification.getId());

            // Publish to RabbitMQ instead of sending email directly
            log.info("📤 Publishing notification to RabbitMQ queue...");
            notificationProducer.sendNotification(request);

            log.info("✅ Notification queued successfully for async processing");

            return notificationMapper.toDto(notification);

        } catch (Exception e) {
            log.error("❌ Failed to queue notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to queue notification", e);
        }
    }

    // CORRECTED VERSION - Remove direct email sending, let RabbitMQ handle it

    @Override
    @Transactional
    public List<NotificationDto> notifyNewProperty(NotifyNewPropertyRequest request) {
        log.info("🏠 Notifying users about new property ID: {}", request.getPropertyId());

        List<NotificationDto> notifications = new ArrayList<>();

        try {
            // Fetch property details
            PropertyServiceResponse propertyResponse = propertyServiceClient
                    .getPropertyById(request.getPropertyId());

            if (propertyResponse == null || propertyResponse.getPropertyId() == null) {
                throw new ResourceNotFoundException("Property not found with ID: " + request.getPropertyId());
            }

            PropertyDto property = mapToPropertyDto(propertyResponse);
            log.info("✅ Property fetched: {}", property.getTitle());

            List<Long> userIds = request.getUserIds();
            if (userIds == null || userIds.isEmpty()) {
                log.warn("⚠️ No specific users provided for property notification.");
                return notifications;
            }

            log.info("📤 Publishing property notifications to RabbitMQ for {} users", userIds.size());

            // ✅ FETCH USER DATA BEFORE SENDING TO RABBITMQ
            for (Long userId : userIds) {
                try {
                    // Fetch user WITH JWT token (in REST request context)
                    UserDto user = userServiceClient.getUsersById(userId);

                    if (user == null || user.getEmail() == null) {
                        log.error("❌ User not found or no email for user ID: {}", userId);
                        continue;
                    }

                    log.info("👤 User found: {} <{}>", user.getUsername(), user.getEmail());

                    // Create notification record as PENDING
                    SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                            .userId(user.getId())
                            .userEmail(user.getEmail())
                            .userName(user.getFirstName() + " " + user.getLastName())
                            .type(NotificationType.NEW_PROPERTY)
                            .subject("🏠 New Property Available: " + property.getTitle())
                            .message(buildPropertyMessage(property))
                            .propertyId(property.getId())
                            .propertyTitle(property.getTitle())
                            .propertyAddress(property.getAddress())
                            .propertyImageUrl(getPropertyImageUrl(property, 0))
                            .propertyUrl("http://localhost:5173/property/" + property.getId())
                            .propertyPrice(property.getRentAmount())
                            .build();

                    Notification notification = notificationMapper.fromRequest(notificationRequest);
                    notification.setStatus(NotificationStatus.PENDING);
                    notification = notificationRepository.save(notification);

                    // ✅ SEND TO RABBITMQ WITH USER DATA INCLUDED
                    notificationProducer.sendPropertyNotification(property, userId, user);

                    log.info("✅ Notification queued for user: {} ({})", user.getUsername(), user.getEmail());
                    notifications.add(notificationMapper.toDto(notification));

                } catch (Exception e) {
                    log.error("❌ Failed to notify user ID {}: {}", userId, e.getMessage());
                }
            }

            log.info("🎉 Successfully queued {} notifications about property: {}",
                    notifications.size(), property.getTitle());

        } catch (Exception e) {
            log.error("❌ Failed to notify about new property: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to notify about new property", e);
        }

        return notifications;
    }
    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(Long id) {
        log.info("🔍 Fetching notification with ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId) {
        log.info("🔍 Fetching notifications for user ID: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notificationMapper.toDtoList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByStatus(NotificationStatus status) {
        log.info(" Fetching notifications with status: {}", status);
        List<Notification> notifications = notificationRepository.findByStatusOrderByCreatedAtDesc(status);
        return notificationMapper.toDtoList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByType(NotificationType type) {
        log.info(" Fetching notifications of type: {}", type);
        List<Notification> notifications = notificationRepository.findByTypeOrderByCreatedAtDesc(type);
        return notificationMapper.toDtoList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications() {
        log.info("📋 Fetching all notifications");
        List<Notification> notifications = notificationRepository.findAll();
        return notificationMapper.toDtoList(notifications);
    }

    @Override
    @Transactional
    public List<NotificationDto> retryFailedNotifications() {
        log.info("🔄 Retrying failed notifications via RabbitMQ");

        List<Notification> failedNotifications = notificationRepository.findPendingOrRetryable(3);
        List<NotificationDto> retriedNotifications = new ArrayList<>();

        for (Notification notification : failedNotifications) {
            try {
                // Create request from notification
                SendNotificationRequest request = SendNotificationRequest.builder()
                        .userId(notification.getUserId())
                        .userEmail(notification.getUserEmail())
                        .userName(notification.getUserName())
                        .type(notification.getType())
                        .subject(notification.getSubject())
                        .message(notification.getMessage())
                        .htmlContent(notification.getHtmlContent())
                        .build();

                // Re-queue via RabbitMQ
                notificationProducer.sendNotification(request);

                notification.setStatus(NotificationStatus.RETRYING);
                notification.setRetryCount(notification.getRetryCount() + 1);

                log.info("✅ Notification ID {} re-queued for retry", notification.getId());
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                log.error("❌ Failed to retry notification ID: {}", notification.getId());
            }

            notification = notificationRepository.save(notification);
            retriedNotifications.add(notificationMapper.toDto(notification));
        }

        log.info("🎉 Re-queued {} notifications for retry", retriedNotifications.size());
        return retriedNotifications;
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(Long id) {
        log.info("✓ Marking notification as read: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        notification.setUpdatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        log.info("🗑️ Deleting notification with ID: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with ID: " + id);
        }
        notificationRepository.deleteById(id);
        log.info("✅ Notification deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatistics getStatistics() {
        log.info("📊 Calculating notification statistics");

        return NotificationStatistics.builder()
                .totalNotifications(notificationRepository.count())
                .pendingNotifications(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .sentNotifications(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failedNotifications(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .retryingNotifications(notificationRepository.countByStatus(NotificationStatus.RETRYING))
                .build();
    }

    @Override
    @Transactional
    public PropertyServiceResponse updatePropertyIsRented(Long propertyId, Boolean isRented) {
        log.info("🔄 Updating property rental status for property ID: {} to {}", propertyId, isRented);

        try {
            PropertyServiceResponse existingProperty = propertyServiceClient.getPropertyById(propertyId);

            if (existingProperty == null || existingProperty.getPropertyId() == null) {
                log.error("❌ Property not found with ID: {}", propertyId);
                throw new ResourceNotFoundException("Property not found with ID: " + propertyId);
            }

            UpdatePropertyRentedRequest patchRequest = UpdatePropertyRentedRequest.builder()
                    .propertyIsRented(isRented)
                    .build();

            PropertyServiceResponse updatedResponse = propertyServiceClient.updateProperty(propertyId, patchRequest);

            log.info("✅ Property rental status updated successfully for property ID: {}", propertyId);
            return updatedResponse;

        } catch (Exception e) {
            log.error("❌ Error updating property rental status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update property rental status: " + e.getMessage(), e);
        }
    }

    // ========== HELPER METHODS ==========

    private String buildPropertyMessage(PropertyDto property) {
        StringBuilder message = new StringBuilder();
        message.append("A new property is now available!\n\n");
        message.append("Title: ").append(property.getTitle()).append("\n");
        message.append("Address: ").append(property.getAddress());

        if (property.getDescription() != null && !property.getDescription().isEmpty()) {
            message.append(", ").append(property.getDescription());
        }
        message.append("\n");

        if (property.getRentAmount() != null) {
            message.append("Price: €").append(property.getRentAmount()).append("/month\n");
        }

        if (property.getBedrooms() != null) {
            message.append("Bedrooms: ").append(property.getBedrooms()).append("\n");
        }

        if (property.getSurfaceArea() != null) {
            message.append("Area: ").append(property.getSurfaceArea()).append("\n");
        }

        message.append("\nView details at: http://localhost:5173/property/").append(property.getId());

        return message.toString();
    }

    private String getPropertyImageUrl(PropertyDto property, int index) {
        if (property.getImages() != null &&
                !property.getImages().isEmpty() &&
                property.getImages().size() > index) {
            return property.getImages().get(index);
        }
        return null;
    }

    private PropertyDto mapToPropertyDto(PropertyServiceResponse response) {
        List<String> images = new ArrayList<>();
        if (response.getImage() != null && !response.getImage().isEmpty()) {
            images.add(response.getImage());
        }
        if (response.getImage2() != null && !response.getImage2().isEmpty()) {
            images.add(response.getImage2());
        }
        if (response.getImage3() != null && !response.getImage3().isEmpty()) {
            images.add(response.getImage3());
        }
        if (response.getImage4() != null && !response.getImage4().isEmpty()) {
            images.add(response.getImage4());
        }

        return PropertyDto.builder()
                .id(response.getPropertyId())
                .title(response.getTitle())
                .description(response.getDescription())
                .address(response.getAddress())
                .propertyType(response.getPropertyType())
                .rentAmount(response.getRentAmount())
                .propertyIsRented(response.getPropertyIsRented())
                .image(response.getImage())
                .image2(response.getImage2())
                .image3(response.getImage3())
                .image4(response.getImage4())
                .images(images)
                .build();
    }
}