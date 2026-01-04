package com.example.notification.model.dto;

import com.example.notification.model.enums.NotificationChannel;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * Message payload for RabbitMQ queue.
 * This is what gets sent to the message queue for async processing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Database ID of the notification record
     */
    private Long notificationId;

    /**
     * User ID receiving the notification
     */
    private Long userId;

    /**
     * Channel for this specific message
     */
    private NotificationChannel channel;

    /**
     * Template code to use
     */
    private String templateCode;

    /**
     * Recipient address (email, phone, token, etc.)
     */
    private String recipient;

    /**
     * Subject line (for EMAIL/PUSH)
     */
    private String subject;

    /**
     * Rendered content to send
     */
    private String renderedContent;

    /**
     * Template data (kept for potential re-rendering on retry)
     */
    private Map<String, Object> templateData;

    /**
     * Current retry attempt number
     */
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Priority level
     */
    @Builder.Default
    private Integer priority = 5;
}
