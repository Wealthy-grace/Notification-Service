package com.example.notificationservice.domain.response;

import com.example.notificationservice.domain.dto.NotificationDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification List Response
 * Response wrapper for list of notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationListResponse {

    private Boolean success;
    private String message;
    private List<NotificationDto> notifications;
    private Integer count;
    private LocalDateTime timestamp;

    public static NotificationListResponse success(String message, List<NotificationDto> notifications) {
        return NotificationListResponse.builder()
                .success(true)
                .message(message)
                .notifications(notifications)
                .count(notifications != null ? notifications.size() : 0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationListResponse error(String message) {
        return NotificationListResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}




























