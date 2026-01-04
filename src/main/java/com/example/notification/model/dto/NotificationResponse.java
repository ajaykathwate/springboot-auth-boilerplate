package com.example.notification.model.dto;

import com.example.notification.model.entity.Notification;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for notification API endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String templateCode;
    private String subject;
    private String renderedContent;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private Integer retryCount;
    private String errorMessage;

    /**
     * Create response from entity
     */
    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setChannel(notification.getChannel());
        response.setStatus(notification.getStatus());
        response.setTemplateCode(notification.getTemplateCode());
        response.setSubject(notification.getSubject());
        response.setRenderedContent(notification.getRenderedContent());
        response.setIsRead(notification.getIsRead());
        response.setReadAt(notification.getReadAt());
        response.setCreatedAt(notification.getCreatedAt());
        response.setDeliveredAt(notification.getDeliveredAt());
        response.setRetryCount(notification.getRetryCount());
        response.setErrorMessage(notification.getErrorMessage());
        return response;
    }
}
