package com.example.notification.provider;

import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.dto.ProviderResponse;

/**
 * Base interface for notification providers.
 * Each channel implementation wraps an external API or service.
 */
public interface NotificationProvider {

    /**
     * Send a notification through this provider.
     *
     * @param message the notification message to send
     * @return the provider response indicating success or failure
     */
    ProviderResponse send(NotificationMessage message);

    /**
     * Check if this provider is enabled and properly configured.
     *
     * @return true if the provider can accept messages
     */
    boolean isEnabled();

    /**
     * Get the provider name for logging and monitoring.
     *
     * @return the provider name
     */
    String getProviderName();
}
