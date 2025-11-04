package com.example.notificationservice.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Notify New Property Request
 * Request to send notifications about new property to multiple users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyNewPropertyRequest {

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    // Optional: specific user IDs to notify
    // If empty, notify all interested users
    private List<Long> userIds;

    // Optional: user roles to notify (e.g., STUDENT, TENANT)
    private List<String> targetRoles;
}