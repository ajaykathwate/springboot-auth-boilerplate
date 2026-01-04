package com.example.notification.publisher;

import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.enums.NotificationChannel;

/**
 * Strategy interface for publishing notifications to channel-specific queues.
 */
public interface ChannelPublisher {

    /**
     * Publish a notification message to the queue.
     *
     * @param message the notification message to publish
     */
    void publish(NotificationMessage message);

    /**
     * Get the channel this publisher handles.
     *
     * @return the notification channel
     */
    NotificationChannel getChannel();

    /**
     * Check if this publisher supports the given channel.
     *
     * @param channel the channel to check
     * @return true if this publisher handles the channel
     */
    default boolean supports(NotificationChannel channel) {
        return getChannel() == channel;
    }
}
