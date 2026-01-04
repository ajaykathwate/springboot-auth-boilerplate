package com.example.notification.controller;

import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.notification.model.dto.NotificationResponse;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.service.NotificationService;
import com.example.security.principal.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for notification endpoints.
 * Provides GET endpoints for fetching user notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "User notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final ApiSuccessResponseCreator responseCreator;

    /**
     * Get user's notifications with pagination and optional filtering.
     */
    @GetMapping
    @Operation(summary = "Get user notifications",
            description = "Get paginated list of user notifications with optional channel filter")
    public ResponseEntity<ApiSuccessResponse> getNotifications(
            @AuthenticationPrincipal SecurityUser user,
            @Parameter(description = "Filter by channel (EMAIL, SMS, WHATSAPP, PUSH, IN_APP)")
            @RequestParam(required = false) NotificationChannel channel,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponse> notifications;

        if (channel != null) {
            notifications = notificationService.getUserNotifications(user.getUserId(), channel, pageable);
        } else {
            notifications = notificationService.getUserNotifications(user.getUserId(), pageable);
        }

        return ResponseEntity.ok(responseCreator.buildResponse(
                "Notifications retrieved successfully",
                true,
                HttpStatus.OK,
                notifications
        ));
    }

    /**
     * Get a specific notification by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID",
            description = "Get a specific notification by its ID")
    public ResponseEntity<ApiSuccessResponse> getNotification(
            @AuthenticationPrincipal SecurityUser user,
            @Parameter(description = "Notification ID")
            @PathVariable Long id) {

        NotificationResponse notification = notificationService.getNotification(id, user.getUserId());

        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    responseCreator.buildResponse("Notification not found", false, HttpStatus.NOT_FOUND)
            );
        }

        return ResponseEntity.ok(responseCreator.buildResponse(
                "Notification retrieved successfully",
                true,
                HttpStatus.OK,
                notification
        ));
    }

    /**
     * Get unread in-app notifications.
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications",
            description = "Get paginated list of unread in-app notifications")
    public ResponseEntity<ApiSuccessResponse> getUnreadNotifications(
            @AuthenticationPrincipal SecurityUser user,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponse> notifications =
                notificationService.getUnreadNotifications(user.getUserId(), pageable);

        return ResponseEntity.ok(responseCreator.buildResponse(
                "Unread notifications retrieved successfully",
                true,
                HttpStatus.OK,
                notifications
        ));
    }

    /**
     * Get count of unread notifications.
     */
    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count",
            description = "Get count of unread in-app notifications")
    public ResponseEntity<ApiSuccessResponse> getUnreadCount(
            @AuthenticationPrincipal SecurityUser user) {

        long count = notificationService.getUnreadCount(user.getUserId());

        return ResponseEntity.ok(responseCreator.buildResponse(
                "Unread count retrieved successfully",
                true,
                HttpStatus.OK,
                Map.of("count", count)
        ));
    }

    /**
     * Mark a notification as read.
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read",
            description = "Mark a specific notification as read")
    public ResponseEntity<ApiSuccessResponse> markAsRead(
            @AuthenticationPrincipal SecurityUser user,
            @Parameter(description = "Notification ID")
            @PathVariable Long id) {

        boolean success = notificationService.markAsRead(id, user.getUserId());

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    responseCreator.buildResponse("Notification not found", false, HttpStatus.NOT_FOUND)
            );
        }

        return ResponseEntity.ok(responseCreator.buildResponse(
                "Notification marked as read",
                true,
                HttpStatus.OK,
                Map.of("success", true)
        ));
    }

    /**
     * Mark all notifications as read.
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read",
            description = "Mark all in-app notifications as read for the user")
    public ResponseEntity<ApiSuccessResponse> markAllAsRead(
            @AuthenticationPrincipal SecurityUser user) {

        int count = notificationService.markAllAsRead(user.getUserId());

        return ResponseEntity.ok(responseCreator.buildResponse(
                "All notifications marked as read",
                true,
                HttpStatus.OK,
                Map.of("markedCount", count)
        ));
    }
}
