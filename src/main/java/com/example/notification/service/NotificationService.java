package com.example.notification.service;

import com.example.notification.model.dto.NotificationRequest;
import com.example.notification.model.dto.NotificationResponse;
import com.example.notification.model.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Main notification service interface.
 * Entry point for sending notifications across all channels.
 */
public interface NotificationService {

    /**
     * Send a multi-channel notification.
     * Fire-and-forget pattern - returns immediately after queuing.
     *
     * @param request the notification request with channels and data
     * @return list of notification IDs created (one per channel)
     */
    List<Long> send(NotificationRequest request);

    /**
     * Send an email notification (convenience method).
     *
     * @param userId       the user receiving the notification
     * @param email        the recipient email address
     * @param templateCode the template to use
     * @param templateData the data to merge with template
     * @return the notification ID
     */
    Long sendEmail(Long userId, String email, String templateCode, Map<String, Object> templateData);

    /**
     * Send an email with subject (convenience method).
     *
     * @param userId       the user receiving the notification
     * @param email        the recipient email address
     * @param templateCode the template to use
     * @param subject      the email subject
     * @param templateData the data to merge with template
     * @return the notification ID
     */
    Long sendEmail(Long userId, String email, String templateCode, String subject, Map<String, Object> templateData);

    /**
     * Send an SMS notification (convenience method).
     *
     * @param userId       the user receiving the notification
     * @param phone        the recipient phone number
     * @param templateCode the template to use
     * @param templateData the data to merge with template
     * @return the notification ID
     */
    Long sendSms(Long userId, String phone, String templateCode, Map<String, Object> templateData);

    /**
     * Send a WhatsApp notification (convenience method).
     *
     * @param userId       the user receiving the notification
     * @param phone        the recipient phone number
     * @param templateCode the template to use
     * @param templateData the data to merge with template
     * @return the notification ID
     */
    Long sendWhatsApp(Long userId, String phone, String templateCode, Map<String, Object> templateData);

    /**
     * Send a push notification (convenience method).
     *
     * @param userId       the user receiving the notification
     * @param fcmToken     the FCM token
     * @param templateCode the template to use
     * @param title        the notification title
     * @param templateData the data to merge with template
     * @return the notification ID
     */
    Long sendPush(Long userId, String fcmToken, String templateCode, String title, Map<String, Object> templateData);

    /**
     * Send an in-app notification (convenience method).
     *
     * @param userId       the user receiving the notification
     * @param templateCode the template to use
     * @param templateData the data to merge with template
     * @return the notification ID
     */
    Long sendInApp(Long userId, String templateCode, Map<String, Object> templateData);

    /**
     * Get user's notifications with pagination.
     *
     * @param userId   the user ID
     * @param pageable pagination parameters
     * @return page of notifications
     */
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Get user's notifications filtered by channel.
     *
     * @param userId   the user ID
     * @param channel  the channel to filter by
     * @param pageable pagination parameters
     * @return page of notifications
     */
    Page<NotificationResponse> getUserNotifications(Long userId, NotificationChannel channel, Pageable pageable);

    /**
     * Get user's unread in-app notifications.
     *
     * @param userId   the user ID
     * @param pageable pagination parameters
     * @return page of unread notifications
     */
    Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Get a specific notification by ID (with security check).
     *
     * @param notificationId the notification ID
     * @param userId         the user ID (for security)
     * @return the notification or null if not found/not owned
     */
    NotificationResponse getNotification(Long notificationId, Long userId);

    /**
     * Get count of unread in-app notifications.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    long getUnreadCount(Long userId);

    /**
     * Mark a notification as read.
     *
     * @param notificationId the notification ID
     * @param userId         the user ID (for security)
     * @return true if marked successfully
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * Mark all in-app notifications as read for a user.
     *
     * @param userId the user ID
     * @return count of notifications marked as read
     */
    int markAllAsRead(Long userId);
}
