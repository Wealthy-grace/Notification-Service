package com.example.notificationservice.producer;
import com.example.notificationservice.domain.dto.NotificationMessage;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.domain.dto.UserDto;
import com.example.notificationservice.domain.request.SendNotificationRequest;
import com.example.notificationservice.persistence.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RabbitMQ Message Producer
 * Publishes notification messages to RabbitMQ queues
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchange;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing-key.property-notification}")
    private String propertyNotificationRoutingKey;

    @Value("${rabbitmq.notification.max-retries:3}")
    private Integer maxRetries;

    /**
     * Send notification message to queue
     */
    public void sendNotification(SendNotificationRequest request) {
        try {
            log.info(" Publishing notification message for user: {} to RabbitMQ", request.getUserEmail());

            NotificationMessage message = NotificationMessage.builder()
                    .userId(request.getUserId())
                    .userEmail(request.getUserEmail())
                    .userName(request.getUserName())
                    .type(request.getType())
                    .subject(request.getSubject())
                    .message(request.getMessage())
                    .htmlContent(request.getHtmlContent())
                    .propertyId(request.getPropertyId())
                    .propertyTitle(request.getPropertyTitle())
                    .propertyAddress(request.getPropertyAddress())
                    .propertyPrice(request.getPropertyPrice())
                    .propertyImageUrl(request.getPropertyImageUrl())
                    .propertyUrl(request.getPropertyUrl())
                    .timestamp(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetries(maxRetries)
                    .build();

            rabbitTemplate.convertAndSend(
                    notificationExchange,
                    notificationRoutingKey,
                    message
            );

            log.info(" Notification message published successfully for user: {}", request.getUserEmail());

        } catch (Exception e) {
            log.error("Failed to publish notification message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish notification message", e);
        }
    }

    /**
     * Send email notification message
     */
    public void sendEmailNotification(String to, String subject, String body, String htmlContent) {
        try {
            log.info(" Publishing email message to: {} via RabbitMQ", to);

            NotificationMessage message = NotificationMessage.builder()
                    .userEmail(to)
                    .subject(subject)
                    .message(body)
                    .htmlContent(htmlContent)
                    .type(NotificationType.NEW_PROPERTY)
                    .timestamp(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetries(maxRetries)
                    .build();

            rabbitTemplate.convertAndSend(
                    notificationExchange,
                    emailRoutingKey,
                    message
            );

            log.info(" Email message published successfully to: {}", to);

        } catch (Exception e) {
            log.error(" Failed to publish email message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish email message", e);
        }
    }

    /**
     * Send property notification to multiple users
     */
    /**
     * Send property notification with user data
     */
    public void sendPropertyNotification(PropertyDto property, Long userId, UserDto user) {
        try {
            log.info(" Publishing property notification for property ID: {} to user: {}",
                    property.getId(), user.getEmail());

            NotificationMessage message = NotificationMessage.builder()
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .userName(user.getUsername())
                    .userFirstName(user.getFullName())  // ✅ Include user data
                    //.userLastName(user.getLastName())    // ✅ Include user data
                    .type(NotificationType.NEW_PROPERTY)
                    .propertyId(property.getId())
                    .propertyTitle(property.getTitle())
                    .propertyAddress(property.getAddress())
                    .propertyPrice(property.getRentAmount())
                    .propertyImageUrl(getFirstImage(property))
                    .propertyImageUrl2(property.getImage2())
                    .propertyImageUrl3(property.getImage3())
                    .propertyUrl("http://localhost:5173/property/" + property.getId())
                    .timestamp(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetries(maxRetries)
                    .build();

            rabbitTemplate.convertAndSend(
                    notificationExchange,
                    propertyNotificationRoutingKey,
                    message
            );

            log.info(" Property notification message published for user: {}", user.getEmail());

        } catch (Exception e) {
            log.error(" Failed to publish property notification message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish property notification message", e);
        }
    }
    /**
     * Retry failed notification
     */
    public void retryNotification(NotificationMessage message) {
        try {
            if (message.getRetryCount() >= message.getMaxRetries()) {
                log.error("⚠️ Max retries reached for notification. Moving to DLQ.");
                return;
            }

            log.info(" Retrying notification (attempt {}/{})",
                    message.getRetryCount() + 1, message.getMaxRetries());

            message.setRetryCount(message.getRetryCount() + 1);
            message.setTimestamp(LocalDateTime.now());

            rabbitTemplate.convertAndSend(
                    notificationExchange,
                    notificationRoutingKey,
                    message
            );

            log.info(" Retry message published successfully");

        } catch (Exception e) {
            log.error(" Failed to retry notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Helper method to get first property image
     */
    private String getFirstImage(PropertyDto property) {
        if (property.getImages() != null && !property.getImages().isEmpty()) {
            return property.getImages().get(0);
        }
        return property.getImage();
    }
}