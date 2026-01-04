package com.example.notification.model.entity;

import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Core entity for tracking notification lifecycle across all channels.
 * Stores every notification attempt with full audit trail.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_created", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_notification_user_channel", columnList = "user_id, channel"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notification_status_created", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user receiving this notification
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Notification channel (EMAIL, SMS, WHATSAPP, PUSH, IN_APP)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    /**
     * Current status in the notification lifecycle
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    /**
     * Template code used for this notification
     */
    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    /**
     * Recipient address (email, phone number, push token) - null for IN_APP
     */
    @Column(length = 255)
    private String recipient;

    /**
     * Subject line (for EMAIL and PUSH)
     */
    @Column(length = 255)
    private String subject;

    /**
     * Rendered content that was sent to the user
     */
    @Column(name = "rendered_content", columnDefinition = "TEXT")
    private String renderedContent;

    /**
     * Original template data as JSON string for audit
     */
    @Column(name = "template_data", columnDefinition = "TEXT")
    private String templateData;

    /**
     * Number of retry attempts
     */
    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * Last error message if failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Error code from provider (if any)
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /**
     * Provider response as JSON for debugging
     */
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    /**
     * External message ID from provider (for tracking)
     */
    @Column(name = "external_id", length = 255)
    private String externalId;

    /**
     * For IN_APP: whether notification has been read
     */
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * Timestamp when notification was read (IN_APP only)
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Custom metadata as JSON (for tracking, analytics, etc.)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * When the notification was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the notification was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * When the notification was sent to the provider
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * When the notification was delivered (confirmed by provider)
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * When the notification failed permanently
     */
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    /**
     * Next scheduled retry time (for RETRY status)
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment retry count and update next retry time
     */
    public void incrementRetry(LocalDateTime nextRetryTime) {
        this.retryCount++;
        this.nextRetryAt = nextRetryTime;
        this.status = NotificationStatus.RETRY;
    }

    /**
     * Mark as delivered
     */
    public void markDelivered(String externalId, String providerResponse) {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.externalId = externalId;
        this.providerResponse = providerResponse;
    }

    /**
     * Mark as permanently failed
     */
    public void markFailed(String errorMessage, String errorCode, NotificationStatus failureStatus) {
        this.status = failureStatus;
        this.failedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    /**
     * Mark as read (for IN_APP notifications)
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
