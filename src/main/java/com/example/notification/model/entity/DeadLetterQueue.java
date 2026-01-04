package com.example.notification.model.entity;

import com.example.notification.model.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for storing notifications that failed after maximum retry attempts.
 * Provides permanent record of failures for analysis and debugging.
 */
@Entity
@Table(name = "notification_dead_letter_queue", indexes = {
        @Index(name = "idx_dlq_notification_id", columnList = "notification_id"),
        @Index(name = "idx_dlq_channel", columnList = "channel"),
        @Index(name = "idx_dlq_created_at", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the original notification
     */
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    /**
     * User ID for quick filtering
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Channel that failed
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    /**
     * Template code used
     */
    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    /**
     * Recipient address
     */
    @Column(length = 255)
    private String recipient;

    /**
     * Original template data as JSON
     */
    @Column(name = "template_data", columnDefinition = "TEXT")
    private String templateData;

    /**
     * Total number of retry attempts made
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * Final failure reason
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT", nullable = false)
    private String failureReason;

    /**
     * Last error code from provider
     */
    @Column(name = "last_error_code", length = 50)
    private String lastErrorCode;

    /**
     * Last provider response as JSON
     */
    @Column(name = "last_provider_response", columnDefinition = "TEXT")
    private String lastProviderResponse;

    /**
     * Complete error history as JSON array
     */
    @Column(name = "error_history", columnDefinition = "TEXT")
    private String errorHistory;

    /**
     * When the original notification was created
     */
    @Column(name = "original_created_at", nullable = false)
    private LocalDateTime originalCreatedAt;

    /**
     * When this DLQ entry was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
