package com.example.notification.service.impl;

import com.example.notification.model.dto.*;
import com.example.notification.model.entity.Notification;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import com.example.notification.publisher.ChannelPublisher;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.service.NotificationService;
import com.example.notification.service.RateLimiterService;
import com.example.notification.template.TemplateRenderer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of NotificationService.
 * Orchestrates the notification sending flow:
 * 1. Rate limit check
 * 2. Template rendering
 * 3. Database persistence
 * 4. Queue publishing
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final RateLimiterService rateLimiterService;
    private final TemplateRenderer templateRenderer;
    private final Map<NotificationChannel, ChannelPublisher> publishers;
    private final ObjectMapper objectMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            RateLimiterService rateLimiterService,
            TemplateRenderer templateRenderer,
            List<ChannelPublisher> channelPublishers,
            ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.rateLimiterService = rateLimiterService;
        this.templateRenderer = templateRenderer;
        this.objectMapper = objectMapper;

        // Build publisher map for quick lookup
        this.publishers = new EnumMap<>(NotificationChannel.class);
        for (ChannelPublisher publisher : channelPublishers) {
            publishers.put(publisher.getChannel(), publisher);
        }
    }

    @Override
    @Transactional
    public List<Long> send(NotificationRequest request) {
        log.info("Sending notification to user {} via {} channels",
                request.getUserId(), request.getChannels().size());

        List<Long> notificationIds = new ArrayList<>();

        for (NotificationChannel channel : request.getChannels()) {
            try {
                Long notificationId = sendToChannel(request, channel);
                if (notificationId != null) {
                    notificationIds.add(notificationId);
                }
            } catch (RateLimitExceededException e) {
                log.warn("Rate limit exceeded for user {} on channel {}: {}",
                        request.getUserId(), channel, e.getMessage());
                // Continue with other channels
            } catch (Exception e) {
                log.error("Failed to send notification to channel {}: {}",
                        channel, e.getMessage(), e);
                // Continue with other channels
            }
        }

        return notificationIds;
    }

    @Override
    @Transactional
    public Long sendEmail(Long userId, String email, String templateCode, Map<String, Object> templateData) {
        return sendEmail(userId, email, templateCode, null, templateData);
    }

    @Override
    @Transactional
    public Long sendEmail(Long userId, String email, String templateCode, String subject, Map<String, Object> templateData) {
        NotificationRequest request = NotificationRequest.builder()
                .channels(List.of(NotificationChannel.EMAIL))
                .userId(userId)
                .templateCode(templateCode)
                .subject(subject)
                .recipientDetails(RecipientDetails.builder().email(email).build())
                .templateData(templateData)
                .build();

        List<Long> ids = send(request);
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Override
    @Transactional
    public Long sendSms(Long userId, String phone, String templateCode, Map<String, Object> templateData) {
        NotificationRequest request = NotificationRequest.builder()
                .channels(List.of(NotificationChannel.SMS))
                .userId(userId)
                .templateCode(templateCode)
                .recipientDetails(RecipientDetails.builder().phone(phone).build())
                .templateData(templateData)
                .build();

        List<Long> ids = send(request);
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Override
    @Transactional
    public Long sendWhatsApp(Long userId, String phone, String templateCode, Map<String, Object> templateData) {
        NotificationRequest request = NotificationRequest.builder()
                .channels(List.of(NotificationChannel.WHATSAPP))
                .userId(userId)
                .templateCode(templateCode)
                .recipientDetails(RecipientDetails.builder().whatsappNumber(phone).build())
                .templateData(templateData)
                .build();

        List<Long> ids = send(request);
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Override
    @Transactional
    public Long sendPush(Long userId, String fcmToken, String templateCode, String title, Map<String, Object> templateData) {
        NotificationRequest request = NotificationRequest.builder()
                .channels(List.of(NotificationChannel.PUSH))
                .userId(userId)
                .templateCode(templateCode)
                .subject(title)
                .recipientDetails(RecipientDetails.builder().fcmToken(fcmToken).build())
                .templateData(templateData)
                .build();

        List<Long> ids = send(request);
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Override
    @Transactional
    public Long sendInApp(Long userId, String templateCode, Map<String, Object> templateData) {
        NotificationRequest request = NotificationRequest.builder()
                .channels(List.of(NotificationChannel.IN_APP))
                .userId(userId)
                .templateCode(templateCode)
                .templateData(templateData)
                .build();

        List<Long> ids = send(request);
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, NotificationChannel channel, Pageable pageable) {
        return notificationRepository
                .findByUserIdAndChannelOrderByCreatedAtDesc(userId, channel, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdAndChannelAndIsReadFalseOrderByCreatedAtDesc(userId, NotificationChannel.IN_APP, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        return notification != null ? NotificationResponse.fromEntity(notification) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndChannelAndIsReadFalse(userId, NotificationChannel.IN_APP);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        if (notification == null) {
            return false;
        }

        notification.markAsRead();
        notificationRepository.save(notification);
        log.debug("Marked notification {} as read for user {}", notificationId, userId);
        return true;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsReadForUser(
                userId,
                NotificationChannel.IN_APP,
                LocalDateTime.now()
        );
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }

    /**
     * Send notification to a specific channel.
     */
    private Long sendToChannel(NotificationRequest request, NotificationChannel channel)
            throws RateLimitExceededException {

        Long userId = request.getUserId();

        // Check rate limit (unless skipped)
        if (!Boolean.TRUE.equals(request.getSkipRateLimit())) {
            if (!rateLimiterService.isAllowed(userId, channel)) {
                throw new RateLimitExceededException(
                        String.format("Rate limit exceeded for user %d on channel %s", userId, channel)
                );
            }
        }

        // Get recipient for this channel
        String recipient = null;
        if (request.getRecipientDetails() != null) {
            recipient = request.getRecipientDetails().getRecipientForChannel(channel.name());
        }

        // Validate recipient (except for IN_APP which doesn't need one)
        if (channel != NotificationChannel.IN_APP && (recipient == null || recipient.isBlank())) {
            log.warn("No recipient provided for {} notification to user {}", channel, userId);
            return null;
        }

        // Render template
        String renderedContent = templateRenderer.render(
                channel,
                request.getTemplateCode(),
                request.getTemplateData()
        );

        // Serialize template data for audit
        String templateDataJson = null;
        if (request.getTemplateData() != null) {
            try {
                templateDataJson = objectMapper.writeValueAsString(request.getTemplateData());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize template data: {}", e.getMessage());
            }
        }

        // Serialize metadata
        String metadataJson = null;
        if (request.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(request.getMetadata());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata: {}", e.getMessage());
            }
        }

        // Create notification entity
        Notification notification = Notification.builder()
                .userId(userId)
                .channel(channel)
                .status(NotificationStatus.PENDING)
                .templateCode(request.getTemplateCode())
                .recipient(recipient)
                .subject(request.getSubject())
                .renderedContent(renderedContent)
                .templateData(templateDataJson)
                .metadata(metadataJson)
                .isRead(false)
                .retryCount(0)
                .build();

        // Save to database
        notification = notificationRepository.save(notification);
        Long notificationId = notification.getId();

        log.debug("Created {} notification {} for user {}", channel, notificationId, userId);

        // Record rate limit attempt
        if (!Boolean.TRUE.equals(request.getSkipRateLimit())) {
            rateLimiterService.recordAttempt(userId, channel);
        }

        // Build message for queue
        NotificationMessage message = NotificationMessage.builder()
                .notificationId(notificationId)
                .userId(userId)
                .channel(channel)
                .templateCode(request.getTemplateCode())
                .recipient(recipient)
                .subject(request.getSubject())
                .renderedContent(renderedContent)
                .templateData(request.getTemplateData())
                .priority(request.getPriority())
                .retryCount(0)
                .build();

        // Publish to queue
        ChannelPublisher publisher = publishers.get(channel);
        if (publisher != null) {
            publisher.publish(message);
        } else {
            log.error("No publisher found for channel {}", channel);
        }

        return notificationId;
    }

    /**
     * Exception for rate limit exceeded.
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
