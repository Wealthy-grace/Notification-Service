package com.example.notificationservice.business.interfaces;


import com.example.notificationservice.domain.dto.NotificationDto;
import com.example.notificationservice.domain.dto.PropertyDto;
import com.example.notificationservice.domain.request.NotifyNewPropertyRequest;
import com.example.notificationservice.domain.request.SendNotificationRequest;
import com.example.notificationservice.domain.response.PropertyServiceResponse;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;

import java.util.List;

/**
 * Notification Service Interface
 * Defines business logic for notification management
 */
public interface NotificationService {

    /**
     * Send a single notification
     */
    NotificationDto sendNotification(SendNotificationRequest request);

    /**
     * Notify about new property to multiple users
     */
    List<NotificationDto> notifyNewProperty(NotifyNewPropertyRequest request);

    /**
     * Get notification by ID
     */
    NotificationDto getNotificationById(Long id);

    /**
     * Get all notifications for a user
     */
    List<NotificationDto> getUserNotifications(Long userId);

    /**
     * Get notifications by status
     */
    List<NotificationDto> getNotificationsByStatus(NotificationStatus status);

    /**
     * Get notifications by type
     */
    List<NotificationDto> getNotificationsByType(NotificationType type);

    /**
     * Get all notifications (admin)
     */
    List<NotificationDto> getAllNotifications();

    /**
     * Retry failed notifications
     */
    List<NotificationDto> retryFailedNotifications();

    /**
     * Mark notification as read
     */
    NotificationDto markAsRead(Long id);

    /**
     * Delete notification
     */
    void deleteNotification(Long id);

    /**
     * Get notification statistics
     */
    NotificationStatistics getStatistics();


    // Update Property Status Is Rented
    PropertyServiceResponse updatePropertyIsRented(Long propertyId, Boolean isRented);
}




























