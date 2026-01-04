package com.example.notification.model.enums;

/**
 * Classification of errors for retry logic.
 */
public enum ErrorType {
    /**
     * Error is temporary and notification can be retried.
     * Examples: network timeout, rate limiting, temporary provider errors
     */
    RETRIABLE,

    /**
     * Error is permanent and notification should not be retried.
     * Examples: invalid recipient, blocked user, malformed content
     */
    PERMANENT
}
