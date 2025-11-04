package com.example.notificationservice.domain.dto;

import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private NotificationType type;
    private String subject;
    private String message;
    private String htmlContent;

    // Property details
    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private String propertyImageUrl;

    private String propertyImageUrl2;
    private String propertyImageUrl3;
    private Double propertyPrice;

    // Status tracking
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private Integer retryCount;
    private String errorMessage;

    // Metadata
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




























