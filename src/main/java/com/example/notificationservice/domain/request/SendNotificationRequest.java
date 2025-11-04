package com.example.notificationservice.domain.request;

import com.example.notificationservice.persistence.model.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Send Notification Request
 * Request body for sending notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String userEmail;

    private String userName;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private String htmlContent;

    // Property details (optional, for property-related notifications)
    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private String propertyImageUrl;
    private String propertyUrl;
    private String propertyImageUrl2;
    private String propertyImageUrl3;
    private BigDecimal propertyPrice;

    // Additional metadata
    private String metadata;
}




























