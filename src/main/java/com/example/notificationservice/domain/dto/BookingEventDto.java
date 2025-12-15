package com.example.notificationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Booking Event DTO for Notification Service
 * Receives booking events from RabbitMQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Event metadata
    private String eventType;
    private LocalDateTime eventTimestamp;
    private String eventId;

    // Booking details
    private String bookingId;
    private String appointmentId;
    private LocalDateTime bookingDate;
    private LocalDateTime moveInDate;
    private LocalDateTime moveOutDate;
    private Integer bookingDurationMonths;
    private String status;
    private String notes;

    // Property details
    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private Boolean propertyIsRented;
    private String propertyImage;
    private String propertyImage2;
    private String propertyImage3;
    private String propertyImage4;
    private String propertyDescription;

    // User details (Requester/Tenant)
    private Long requesterId;
    private String requesterUsername;
    private String requesterName;
    private String requesterEmail;
    private String requesterPhone;
    private String requesterProfileImage;

    // User details (Provider/Landlord)
    private Long providerId;
    private String providerUsername;
    private String providerName;
    private String providerEmail;
    private String providerPhone;
    private String providerProfileImage;

    // Payment details
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal monthlyRent;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime paymentDeadline;
    private String paymentMethod;
    private String transactionId;
    private String paymentReference;
    private LocalDateTime paymentDate;

    // Cancellation
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    // Contract
    private Boolean contractSigned;
}