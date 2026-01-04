package com.example.notification.repository;

import com.example.notification.model.entity.Notification;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find notifications for a user filtered by channel
     */
    Page<Notification> findByUserIdAndChannelOrderByCreatedAtDesc(
            Long userId, NotificationChannel channel, Pageable pageable);

    /**
     * Find unread in-app notifications for a user
     */
    Page<Notification> findByUserIdAndChannelAndIsReadFalseOrderByCreatedAtDesc(
            Long userId, NotificationChannel channel, Pageable pageable);

    /**
     * Count unread in-app notifications for a user
     */
    long countByUserIdAndChannelAndIsReadFalse(Long userId, NotificationChannel channel);

    /**
     * Find notifications for a user filtered by read status (IN_APP)
     */
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(
            Long userId, Boolean isRead, Pageable pageable);

    /**
     * Find notifications by status (for monitoring/debugging)
     */
    Page<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable pageable);

    /**
     * Find notifications ready for retry (status = RETRY and next_retry_at <= now)
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.nextRetryAt <= :now")
    List<Notification> findNotificationsReadyForRetry(
            @Param("status") NotificationStatus status,
            @Param("now") LocalDateTime now);

    /**
     * Mark all in-app notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now, n.updatedAt = :now " +
            "WHERE n.userId = :userId AND n.channel = :channel AND n.isRead = false")
    int markAllAsReadForUser(
            @Param("userId") Long userId,
            @Param("channel") NotificationChannel channel,
            @Param("now") LocalDateTime now);

    /**
     * Count notifications sent to a user within a time window (for rate limiting backup check)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.channel = :channel " +
            "AND n.createdAt >= :since")
    long countNotificationsSince(
            @Param("userId") Long userId,
            @Param("channel") NotificationChannel channel,
            @Param("since") LocalDateTime since);

    /**
     * Find notification by ID and user ID (for security)
     */
    Notification findByIdAndUserId(Long id, Long userId);

    /**
     * Find notifications by user and multiple channels
     */
    Page<Notification> findByUserIdAndChannelInOrderByCreatedAtDesc(
            Long userId, List<NotificationChannel> channels, Pageable pageable);
}
