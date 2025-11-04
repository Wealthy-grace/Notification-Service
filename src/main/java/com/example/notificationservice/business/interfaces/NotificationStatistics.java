package com.example.notificationservice.business.interfaces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



//Notification Statistics
// Aggregated statistics about notifications
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatistics {
    private Long totalNotifications;
    private Long pendingNotifications;
    private Long sentNotifications;
    private Long failedNotifications;
    private Long retryingNotifications;
}




























