package com.example.notification.model.dto;

import com.example.notification.model.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for sending notifications across multiple channels.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    /**
     * Channels to send notification through
     */
    @NotEmpty(message = "At least one channel must be specified")
    private List<NotificationChannel> channels;

    /**
     * User ID receiving the notification
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Template code to use for rendering content
     */
    @NotBlank(message = "Template code is required")
    private String templateCode;

    /**
     * Recipient contact information
     */
    private RecipientDetails recipientDetails;

    /**
     * Data to merge with template
     */
    private Map<String, Object> templateData;

    /**
     * Custom subject line (overrides template subject for EMAIL/PUSH)
     */
    private String subject;

    /**
     * Custom metadata for tracking
     */
    private Map<String, String> metadata;

    /**
     * Priority level (0-10, higher = more important)
     */
    @Builder.Default
    private Integer priority = 5;

    /**
     * Whether to skip rate limiting check
     */
    @Builder.Default
    private Boolean skipRateLimit = false;
}
