package com.example.notification.template;

import com.example.notification.model.enums.NotificationChannel;

import java.util.Map;

/**
 * Interface for rendering notification templates.
 */
public interface TemplateRenderer {

    /**
     * Render a template with the given data.
     *
     * @param channel      the notification channel
     * @param templateCode the template code/name
     * @param data         the template data to merge
     * @return the rendered content
     */
    String render(NotificationChannel channel, String templateCode, Map<String, Object> data);

    /**
     * Check if a template exists for the given channel and code.
     *
     * @param channel      the notification channel
     * @param templateCode the template code/name
     * @return true if the template exists
     */
    boolean templateExists(NotificationChannel channel, String templateCode);
}
