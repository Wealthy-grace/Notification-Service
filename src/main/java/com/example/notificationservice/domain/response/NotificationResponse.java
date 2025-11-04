package com.example.notificationservice.domain.response;

import com.example.notificationservice.domain.dto.NotificationDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification Response
 * Standard response wrapper for notification operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Boolean success;
    private String message;
    private NotificationDto notification;
    private LocalDateTime timestamp;

    public static NotificationResponse success(String message, NotificationDto notification) {
        return NotificationResponse.builder()
                .success(true)
                .message(message)
                .notification(notification)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationResponse error(String message) {
        return NotificationResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}




























