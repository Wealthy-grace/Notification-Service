package com.example.notificationservice.domain.dto;

import com.example.notificationservice.persistence.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private Long userId;
    private String userEmail;
    private String userName;
    private String userFirstName;  // ✅ ADD THIS
    private String userLastName;   // ✅ ADD THIS

    private NotificationType type;
    private String subject;
    private String message;
    private String htmlContent;

    // Property-related fields
    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private BigDecimal propertyPrice;
    private String propertyImageUrl;
    private String propertyImageUrl2;
    private String propertyImageUrl3;
    private String propertyUrl;

    // Retry mechanism
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime timestamp;
}