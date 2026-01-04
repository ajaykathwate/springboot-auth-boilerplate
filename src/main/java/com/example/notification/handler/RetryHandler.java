package com.example.notification.handler;

import com.example.config.notification.NotificationProperties;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.entity.DeadLetterQueue;
import com.example.notification.model.entity.Notification;
import com.example.notification.model.enums.ErrorType;
import com.example.notification.model.enums.NotificationStatus;
import com.example.notification.repository.DeadLetterQueueRepository;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for retry logic with exponential backoff.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryHandler {

    private final NotificationProperties properties;
    private final NotificationRepository notificationRepository;
    private final DeadLetterQueueRepository deadLetterQueueRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ErrorHandler errorHandler;

    /**
     * Handle a failed notification and determine if it should be retried.
     *
     * @param notification the notification entity
     * @param message      the notification message
     * @param errorMessage the error message
     * @param errorCode    the error code from provider
     * @param errorType    the type of error
     * @return true if the notification will be retried, false if moved to DLQ
     */
    @Transactional
    public boolean handleFailure(
            Notification notification,
            NotificationMessage message,
            String errorMessage,
            String errorCode,
            ErrorType errorType) {

        // Update notification with error info
        notification.setErrorMessage(errorMessage);
        notification.setErrorCode(errorCode);

        // Check if error is permanent
        if (errorType == ErrorType.PERMANENT) {
            log.warn("Permanent failure for notification {}. Moving to DLQ. Error: {}",
                    notification.getId(), errorMessage);
            moveToDlq(notification, "Permanent error: " + errorMessage);
            return false;
        }

        // Check retry count
        int currentRetry = notification.getRetryCount();
        int maxAttempts = properties.getRetry().getMaxAttempts();

        if (currentRetry >= maxAttempts) {
            log.warn("Max retry attempts ({}) reached for notification {}. Moving to DLQ.",
                    maxAttempts, notification.getId());
            moveToDlq(notification, "Max retry attempts reached. Last error: " + errorMessage);
            return false;
        }

        // Calculate next retry time with exponential backoff
        LocalDateTime nextRetryTime = calculateNextRetryTime(currentRetry);

        // Update notification for retry
        notification.incrementRetry(nextRetryTime);
        notificationRepository.save(notification);

        // Schedule retry by re-publishing with delay
        scheduleRetry(message, nextRetryTime, currentRetry + 1);

        log.info("Scheduled retry {} of {} for notification {} at {}",
                currentRetry + 1, maxAttempts, notification.getId(), nextRetryTime);

        return true;
    }

    /**
     * Handle a successful notification delivery.
     */
    @Transactional
    public void handleSuccess(Notification notification, String externalId, String providerResponse) {
        notification.markDelivered(externalId, providerResponse);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("Notification {} delivered successfully. External ID: {}",
                notification.getId(), externalId);
    }

    /**
     * Calculate the next retry time using exponential backoff.
     *
     * @param currentRetryCount the current retry count (0-based)
     * @return the time for the next retry
     */
    private LocalDateTime calculateNextRetryTime(int currentRetryCount) {
        long initialBackoffMs = properties.getRetry().getInitialBackoffMs();
        double multiplier = properties.getRetry().getMultiplier();
        long maxBackoffMs = properties.getRetry().getMaxBackoffMs();

        // Calculate backoff: initialBackoff * multiplier^retryCount
        long backoffMs = (long) (initialBackoffMs * Math.pow(multiplier, currentRetryCount));

        // Cap at maximum backoff
        backoffMs = Math.min(backoffMs, maxBackoffMs);

        return LocalDateTime.now().plusNanos(backoffMs * 1_000_000);
    }

    /**
     * Move a notification to the dead letter queue.
     */
    @Transactional
    public void moveToDlq(Notification notification, String failureReason) {
        // Update notification status
        notification.markFailed(
                failureReason,
                notification.getErrorCode(),
                notification.getRetryCount() >= properties.getRetry().getMaxAttempts()
                        ? NotificationStatus.FAILED_MAX_RETRY
                        : NotificationStatus.FAILED_PERMANENT
        );
        notificationRepository.save(notification);

        // Create DLQ entry
        DeadLetterQueue dlqEntry = DeadLetterQueue.builder()
                .notificationId(notification.getId())
                .userId(notification.getUserId())
                .channel(notification.getChannel())
                .templateCode(notification.getTemplateCode())
                .recipient(notification.getRecipient())
                .templateData(notification.getTemplateData())
                .retryCount(notification.getRetryCount())
                .failureReason(failureReason)
                .lastErrorCode(notification.getErrorCode())
                .lastProviderResponse(notification.getProviderResponse())
                .originalCreatedAt(notification.getCreatedAt())
                .build();
        deadLetterQueueRepository.save(dlqEntry);

        log.warn("Notification {} moved to DLQ. Reason: {}", notification.getId(), failureReason);
    }

    /**
     * Schedule a retry by re-publishing the message.
     * Uses RabbitMQ message TTL for delay.
     */
    private void scheduleRetry(NotificationMessage message, LocalDateTime nextRetryTime, int retryCount) {
        // Update retry count in message
        message.setRetryCount(retryCount);

        // Calculate delay in milliseconds
        long delayMs = java.time.Duration.between(LocalDateTime.now(), nextRetryTime).toMillis();

        // For simplicity, we'll re-publish immediately and let the worker check the retry time
        // In production, you might use RabbitMQ delayed message plugin or separate retry queues
        String routingKey = message.getChannel().getRoutingKey();

        rabbitTemplate.convertAndSend(
                properties.getQueue().getExchange(),
                routingKey,
                message,
                m -> {
                    // Set message header with retry info
                    m.getMessageProperties().setHeader("x-retry-count", retryCount);
                    m.getMessageProperties().setHeader("x-next-retry-at", nextRetryTime.toString());
                    // Set message delay if using delayed message plugin
                    if (delayMs > 0) {
                        m.getMessageProperties().setDelay((int) Math.min(delayMs, Integer.MAX_VALUE));
                    }
                    return m;
                }
        );
    }

    /**
     * Check if a notification should be retried based on its current state.
     */
    public boolean shouldRetry(Notification notification) {
        return notification.getRetryCount() < properties.getRetry().getMaxAttempts()
                && notification.getStatus() == NotificationStatus.RETRY;
    }

    /**
     * Get the current retry configuration.
     */
    public NotificationProperties.RetryConfig getRetryConfig() {
        return properties.getRetry();
    }
}
