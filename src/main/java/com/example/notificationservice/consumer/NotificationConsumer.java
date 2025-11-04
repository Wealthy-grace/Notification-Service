package com.example.notificationservice.consumer;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private final UserServiceClient userServiceClient;
    private final NotificationRepository notificationRepository;

    /**
     * Consume email.queue messages and send email
     */
    @RabbitListener(queues = "${rabbitmq.queue.email}", id = "emailQueueListener")
    @Transactional
    public void consumeEmail(NotificationMessage message) {
        log.info(" EMAIL QUEUE: Received message for: {}", message.getUserEmail());

        try {
            if (message.getHtmlContent() != null && !message.getHtmlContent().isEmpty()) {
                emailService.sendHtmlEmail(
                        message.getUserEmail(),
                        message.getSubject(),
                        message.getHtmlContent()
                );
            } else {
                emailService.sendEmail(
                        message.getUserEmail(),
                        message.getSubject(),
                        message.getMessage()
                );
            }

            log.info("✅ Email sent successfully to: {}", message.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", message.getUserEmail(), e.getMessage(), e);
        }
    }

    /**
     * Consume property-notification.queue messages
     * THIS IS THE KEY LISTENER THAT WAS MISSING!
     */
    /**
     * Consume property-notification.queue messages
     * ✅ NO LONGER NEEDS TO CALL USER SERVICE
     */
    @RabbitListener(queues = "${rabbitmq.queue.property-notification}", id = "propertyNotificationQueueListener")
    @Transactional
    public void consumePropertyNotification(NotificationMessage message) {
        log.info("📥 PROPERTY NOTIFICATION QUEUE: Received message for user: {}", message.getUserEmail());
        log.info("🔍 Message details: propertyId={}, propertyTitle={}",
                message.getPropertyId(), message.getPropertyTitle());

        try {
            // ✅ USE USER DATA FROM MESSAGE (no need to fetch)
            if (message.getUserEmail() == null || message.getUserEmail().isEmpty()) {
                log.error("❌ No email provided in message for user ID: {}", message.getUserId());
                updateNotificationStatus(message.getUserId(), NotificationStatus.FAILED,
                        "No email address in message");
                return;
            }

            log.info("👤 Processing notification for user: {} <{}>",
                    message.getUserName(), message.getUserEmail());

            // Build PropertyDto from message
            PropertyDto property = PropertyDto.builder()
                    .id(message.getPropertyId())
                    .title(message.getPropertyTitle())
                    .address(message.getPropertyAddress())
                    .rentAmount(message.getPropertyPrice())
                    .image(message.getPropertyImageUrl())
                    .image2(message.getPropertyImageUrl2())
                    .image3(message.getPropertyImageUrl3())
                    .build();

            // Update status to PROCESSING
            updateNotificationStatus(message.getUserId(), NotificationStatus.PROCESSING, null);

            // Send property notification email
            String fullName = (message.getUserFirstName() != null ? message.getUserFirstName() : "") +
                    " " +
                    (message.getUserLastName() != null ? message.getUserLastName() : "");
            fullName = fullName.trim();
            if (fullName.isEmpty()) {
                fullName = message.getUserName();
            }

            log.info("📧 Sending property notification email to: {}", message.getUserEmail());
            emailService.sendNewPropertyNotification(
                    message.getUserEmail(),
                    fullName,
                    property
            );

            // Update status to SENT
            updateNotificationStatus(message.getUserId(), NotificationStatus.SENT, null);
            log.info("✅✅✅ Property notification email sent successfully to: {}", message.getUserEmail());

        } catch (Exception e) {
            log.error("❌❌❌ Failed to process property notification for user {}: {}",
                    message.getUserEmail(), e.getMessage(), e);
            updateNotificationStatus(message.getUserId(), NotificationStatus.FAILED,
                    e.getMessage());
        }
    }

    /**
     * Consume general notification.queue messages
     */
    @RabbitListener(queues = "${rabbitmq.queue.notification}", id = "notificationQueueListener")
    @Transactional
    public void consumeNotification(NotificationMessage message) {
        log.info("📥 NOTIFICATION QUEUE: Received message for: {}", message.getUserEmail());

        try {
            updateNotificationStatus(message.getUserId(), NotificationStatus.PROCESSING, null);

            if (message.getHtmlContent() != null && !message.getHtmlContent().isEmpty()) {
                emailService.sendHtmlEmail(
                        message.getUserEmail(),
                        message.getSubject(),
                        message.getHtmlContent()
                );
            } else {
                emailService.sendEmail(
                        message.getUserEmail(),
                        message.getSubject(),
                        message.getMessage()
                );
            }

            updateNotificationStatus(message.getUserId(), NotificationStatus.SENT, null);
            log.info("✅ Notification processed and email sent to: {}", message.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Failed to process notification for {}: {}",
                    message.getUserEmail(), e.getMessage(), e);
            updateNotificationStatus(message.getUserId(), NotificationStatus.FAILED,
                    e.getMessage());
        }
    }

    /**
     * Update notification status in database
     */
    private void updateNotificationStatus(Long userId, NotificationStatus status, String errorMessage) {
        if (userId == null) {
            log.warn("⚠️ Cannot update notification status - userId is null");
            return;
        }

        try {
            notificationRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(
                            userId,
                            Arrays.asList(NotificationStatus.PENDING,
                                    NotificationStatus.PROCESSING,
                                    NotificationStatus.RETRYING)
                    )
                    .ifPresent(notification -> {
                        notification.setStatus(status);
                        notification.setUpdatedAt(LocalDateTime.now());

                        if (errorMessage != null) {
                            notification.setErrorMessage(errorMessage);
                        }

                        if (status == NotificationStatus.SENT) {
                            notification.setSentAt(LocalDateTime.now());
                        }

                        if (status == NotificationStatus.RETRYING) {
                            notification.setRetryCount(notification.getRetryCount() + 1);
                        }

                        notificationRepository.save(notification);
                        log.debug("💾 Notification status updated to: {} for user ID: {}", status, userId);
                    });
        } catch (Exception e) {
            log.error("❌ Failed to update notification status: {}", e.getMessage(), e);
        }
    }
}