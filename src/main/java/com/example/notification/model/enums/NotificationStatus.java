package com.example.notification.model.enums;

/**
 * Status tracking for notification lifecycle.
 */
public enum NotificationStatus {
    /**
     * Notification created but not yet sent to queue
     */
    PENDING,

    /**
     * Notification is being processed by a worker
     */
    PROCESSING,

    /**
     * Notification failed and will be retried
     */
    RETRY,

    /**
     * Notification was successfully delivered
     */
    DELIVERED,

    /**
     * Notification failed permanently (invalid recipient, blocked, etc.)
     */
    FAILED_PERMANENT,

    /**
     * Notification failed after maximum retry attempts
     */
    FAILED_MAX_RETRY
}
