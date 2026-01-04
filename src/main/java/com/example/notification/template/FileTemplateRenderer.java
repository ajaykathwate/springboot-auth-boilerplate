package com.example.notification.template;

import com.example.notification.model.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

/**
 * File-based template renderer using Thymeleaf.
 * Loads templates from the file system and renders them with provided data.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileTemplateRenderer implements TemplateRenderer {

    @Qualifier("notificationTemplateEngine")
    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(NotificationChannel channel, String templateCode, Map<String, Object> data) {
        String templateName = buildTemplatePath(channel, templateCode);

        try {
            Context context = new Context(Locale.getDefault());
            if (data != null) {
                context.setVariables(data);
            }

            String rendered = templateEngine.process(templateName, context);
            log.debug("Successfully rendered template: {} for channel: {}", templateCode, channel);
            return rendered;

        } catch (Exception e) {
            log.error("Failed to render template: {} for channel: {}. Error: {}",
                    templateCode, channel, e.getMessage());
            throw new TemplateRenderException(
                    String.format("Failed to render template '%s' for channel %s", templateCode, channel),
                    e
            );
        }
    }

    @Override
    public boolean templateExists(NotificationChannel channel, String templateCode) {
        String templateName = buildTemplatePath(channel, templateCode);
        try {
            // Try to process a simple check - if template doesn't exist, this will throw
            Context context = new Context(Locale.getDefault());
            templateEngine.process(templateName, context);
            return true;
        } catch (Exception e) {
            log.debug("Template not found: {} for channel: {}", templateCode, channel);
            return false;
        }
    }

    /**
     * Build the template path from channel and template code.
     * Format: {channel}/{templateCode}
     * The file extension is handled by Thymeleaf resolvers.
     */
    private String buildTemplatePath(NotificationChannel channel, String templateCode) {
        return channel.getFolderName() + "/" + templateCode;
    }

    /**
     * Exception thrown when template rendering fails.
     */
    public static class TemplateRenderException extends RuntimeException {
        public TemplateRenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
