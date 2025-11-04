package com.example.notificationservice.persistence.repository;

import com.example.notificationservice.persistence.model.Notification;
import com.example.notificationservice.persistence.model.NotificationStatus;
import com.example.notificationservice.persistence.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);

    List<Notification> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' OR " +
            "(n.status = 'FAILED' AND n.retryCount < :maxRetries)")
    List<Notification> findPendingOrRetryable(@Param("maxRetries") int maxRetries);

    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    Long countByStatus(NotificationStatus status);

    Long countByUserId(Long userId);

    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(org.springframework.data.domain.Pageable pageable);

    /**
     * Only one version returning Optional to avoid clash
     */
    Optional<Notification> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<NotificationStatus> statuses);

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status != 'SENT' ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") Long userId);
}
