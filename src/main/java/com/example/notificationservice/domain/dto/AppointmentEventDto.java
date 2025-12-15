package com.example.notificationservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Appointment Event DTO for Notification Service
 * Receives appointment events from RabbitMQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Event metadata
    private String eventType;
    private LocalDateTime eventTimestamp;
    private String eventId;

    // Appointment details
    private String appointmentId;
    private String appointmentTitle;
    private String description;
    private LocalDateTime appointmentDateTime;
    private Integer durationMinutes;
    private String status;
    private String type;
    private String location;
    private String meetingLink;
    private String notes;

    // Requester details
    private Long requesterId;
    private String requesterUsername;
    private String requesterName;
    private String requesterEmail;
    private String requesterPhone;
    private String requesterFirstName;
    private String requesterLastName;
    private String requesterProfileImage;

    // Provider details
    private Long providerId;
    private String providerUsername;
    private String providerName;
    private String providerEmail;
    private String providerPhone;
    private String providerFirstName;
    private String providerLastName;
    private String providerProfileImage;

    // Property details
    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private Boolean propertyIsRented;
    private String propertyImage;
    private String propertyImage2;
    private String propertyImage3;
    private String propertyImage4;
    private BigDecimal propertyRentAmount;
    private String propertyDescription;

    // Cancellation/Reschedule
    private String cancellationReason;
    private LocalDateTime previousDateTime;
}