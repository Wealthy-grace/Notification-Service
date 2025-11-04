package com.example.notificationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Property notification message payload
 * Used specifically for notifying users about new properties
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PropertyNotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private BigDecimal rentAmount;
    private String imageUrl;
    private Integer bedrooms;
    private String surfaceArea;
    private List<Long> userIds;
    private LocalDateTime timestamp;
    private String htmlContent;
}
