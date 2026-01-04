package com.example.notification.service;

import com.example.notification.model.enums.NotificationChannel;

/**
 * Service for rate limiting notifications per user per channel.
 */
public interface RateLimiterService {

    /**
     * Check if a notification can be sent (within rate limits).
     *
     * @param userId  the user ID
     * @param channel the notification channel
     * @return true if the notification is allowed
     */
    boolean isAllowed(Long userId, NotificationChannel channel);

    /**
     * Record a notification attempt (increment counter).
     *
     * @param userId  the user ID
     * @param channel the notification channel
     */
    void recordAttempt(Long userId, NotificationChannel channel);

    /**
     * Get remaining quota for a user on a specific channel.
     *
     * @param userId  the user ID
     * @param channel the notification channel
     * @return the number of remaining allowed notifications
     */
    int getRemainingQuota(Long userId, NotificationChannel channel);

    /**
     * Get the time in seconds until the rate limit window resets.
     *
     * @param userId  the user ID
     * @param channel the notification channel
     * @return seconds until reset, or -1 if no limit is set
     */
    long getTimeToReset(Long userId, NotificationChannel channel);
}
