package com.example.notificationservice.producer;

import com.example.notificationservice.business.client.UserServiceClient;
import com.example.notificationservice.business.interfaces.EmailService;
import com.example.notificationservice.domain.dto.NotificationMessage;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.domain.dto.UserDto;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final UserServiceClient userServiceClient;
    private final NotificationRepository notificationRepository;
    private final NotificationProducer notificationProducer;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    @Transactional
    public void consumeNotification(NotificationMessage message) {
        log.info("📥 Received notification message for user: {}", message.getUserEmail());

        try {
            updateNotificationStatus(message.getUserId(), NotificationStatus.PROCESSING, null);

            if (message.getHtmlContent() != null && !message.getHtmlContent().isEmpty()) {
                emailService.sendHtmlEmail(message.getUserEmail(), message.getSubject(), message.getHtmlContent());
            } else {
                emailService.sendEmail(message.getUserEmail(), message.getSubject(), message.getMessage());
            }

            updateNotificationStatus(message.getUserId(), NotificationStatus.SENT, null);
            log.info("✅ Notification processed and email sent to: {}", message.getUserEmail());
        } catch (Exception e) {
            log.error("❌ Failed to process notification for {}: {}", message.getUserEmail(), e.getMessage(), e);
            handleNotificationFailure(message, e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    @Transactional
    public void consumeEmail(NotificationMessage message) {
        log.info("📥 Received email message for: {}", message.getUserEmail());

        try {
            if (message.getHtmlContent() != null && !message.getHtmlContent().isEmpty()) {
                emailService.sendHtmlEmail(message.getUserEmail(), message.getSubject(), message.getHtmlContent());
            } else {
                emailService.sendEmail(message.getUserEmail(), message.getSubject(), message.getMessage());
            }

            log.info("✅ Email sent successfully to: {}", message.getUserEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", message.getUserEmail(), e.getMessage(), e);
            handleEmailFailure(message, e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.property-notification}")
    @Transactional
    public void consumePropertyNotification(NotificationMessage message) {
        log.info("📥 Received property notification for user ID: {}", message.getUserId());

        try {
            UserDto user = getUserWithCircuitBreaker(message.getUserId());

            if (user == null || user.getEmail() == null) {
                log.error("❌ User not found or no email for user ID: {}", message.getUserId());
                return;
            }

            PropertyDto property = PropertyDto.builder()
                    .id(message.getPropertyId())
                    .title(message.getPropertyTitle())
                    .address(message.getPropertyAddress())
                    .rentAmount(message.getPropertyPrice())
                    .image(message.getPropertyImageUrl())
                    .image2(message.getPropertyImageUrl2())
                    .image3(message.getPropertyImageUrl3())
                    .build();

            updateNotificationStatus(message.getUserId(), NotificationStatus.PROCESSING, null);

            emailService.sendNewPropertyNotification(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    property
            );

            updateNotificationStatus(message.getUserId(), NotificationStatus.SENT, null);
            log.info("✅ Property notification email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to process property notification for user ID {}: {}",
                    message.getUserId(), e.getMessage(), e);
            handleNotificationFailure(message, e);
        }
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    private UserDto getUserWithCircuitBreaker(Long userId) {
        return userServiceClient.getUsersById(userId);
    }

    private UserDto getUserFallback(Long userId, Exception ex) {
        log.error("⚠️ USER SERVICE CIRCUIT BREAKER ACTIVATED for user ID: {}", userId);
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setEmail("fallback-user-" + userId + "@system.local");
        fallbackUser.setFirstName("System");
        fallbackUser.setLastName("User");
        fallbackUser.setUsername("fallback-user-" + userId);
        return fallbackUser;
    }

    private void updateNotificationStatus(Long userId, NotificationStatus status, String errorMessage) {
        try {
            notificationRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(
                            userId,
                            Arrays.asList(NotificationStatus.PENDING, NotificationStatus.PROCESSING, NotificationStatus.RETRYING)
                    )
                    .ifPresent(notification -> {
                        notification.setStatus(status);
                        notification.setUpdatedAt(LocalDateTime.now());

                        if (errorMessage != null) notification.setErrorMessage(errorMessage);
                        if (status == NotificationStatus.SENT) notification.setSentAt(LocalDateTime.now());
                        if (status == NotificationStatus.RETRYING)
                            notification.setRetryCount(notification.getRetryCount() + 1);

                        notificationRepository.save(notification);
                        log.debug("💾 Notification status updated to: {} for user ID: {}", status, userId);
                    });
        } catch (Exception e) {
            log.error("❌ Failed to update notification status: {}", e.getMessage(), e);
        }
    }

    private void handleNotificationFailure(NotificationMessage message, Exception exception) {
        try {
            if (message.getRetryCount() >= message.getMaxRetries()) {
                updateNotificationStatus(message.getUserId(), NotificationStatus.FAILED,
                        "Max retries reached: " + exception.getMessage());
                return;
            }

            message.setRetryCount(message.getRetryCount() + 1);
            updateNotificationStatus(message.getUserId(), NotificationStatus.RETRYING,
                    "Retry attempt " + message.getRetryCount());

            notificationProducer.retryNotification(message);

        } catch (Exception e) {
            log.error("❌ Failed to handle notification failure: {}", e.getMessage(), e);
        }
    }

    private void handleEmailFailure(NotificationMessage message, Exception exception) {
        log.error("❌ Email delivery failed for: {}. Reason: {}",
                message.getUserEmail(), exception.getMessage());
    }
}
