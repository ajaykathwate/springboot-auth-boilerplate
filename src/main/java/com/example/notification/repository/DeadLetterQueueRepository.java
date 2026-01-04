package com.example.notification.repository;

import com.example.notification.model.entity.DeadLetterQueue;
import com.example.notification.model.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueue, Long> {

    /**
     * Find DLQ entries by notification ID
     */
    DeadLetterQueue findByNotificationId(Long notificationId);

    /**
     * Find DLQ entries for a user
     */
    Page<DeadLetterQueue> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find DLQ entries by channel
     */
    Page<DeadLetterQueue> findByChannelOrderByCreatedAtDesc(NotificationChannel channel, Pageable pageable);

    /**
     * Count DLQ entries by channel (for monitoring)
     */
    long countByChannel(NotificationChannel channel);

    /**
     * Count total DLQ entries (for monitoring)
     */
    long count();
}
