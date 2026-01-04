package com.example.notification.worker;

import com.example.notification.handler.RetryHandler;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.dto.ProviderResponse;
import com.example.notification.model.entity.Notification;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.model.enums.NotificationStatus;
import com.example.notification.provider.NotificationProvider;
import com.example.notification.repository.NotificationRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Abstract base class for notification workers.
 * Implements the Template Method pattern for common processing flow.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class NotificationWorker {

    protected final NotificationRepository notificationRepository;
    protected final RetryHandler retryHandler;

    /**
     * Process a notification message from the queue.
     * This is the template method that defines the processing flow.
     *
     * @param notificationMessage the notification message
     * @param message             the AMQP message
     * @param channel             the AMQP channel
     */
    @Transactional
    public void processMessage(NotificationMessage notificationMessage, Message message, Channel channel) {
        Long notificationId = notificationMessage.getNotificationId();
        log.info("Processing {} notification: {}", getNotificationChannel(), notificationId);

        try {
            // Fetch notification from database
            Notification notification = notificationRepository.findById(notificationId).orElse(null);

            if (notification == null) {
                log.error("Notification {} not found in database. Acknowledging message.", notificationId);
                acknowledgeMessage(channel, message);
                return;
            }

            // Update status to PROCESSING
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);

            // Get the provider and send
            NotificationProvider provider = getProvider();

            if (provider == null || !provider.isEnabled()) {
                log.warn("{} provider is not available. Moving to retry.", getNotificationChannel());
                retryHandler.handleFailure(
                        notification,
                        notificationMessage,
                        "Provider not available",
                        "PROVIDER_UNAVAILABLE",
                        com.example.notification.model.enums.ErrorType.RETRIABLE
                );
                acknowledgeMessage(channel, message);
                return;
            }

            // Send the notification
            ProviderResponse response = provider.send(notificationMessage);

            if (response.isSuccess()) {
                // Handle success
                retryHandler.handleSuccess(
                        notification,
                        response.getMessageId(),
                        response.getRawResponse()
                );
            } else {
                // Handle failure
                retryHandler.handleFailure(
                        notification,
                        notificationMessage,
                        response.getErrorMessage(),
                        response.getErrorCode(),
                        response.getErrorType()
                );
            }

            // Acknowledge the message
            acknowledgeMessage(channel, message);

        } catch (Exception e) {
            log.error("Error processing {} notification {}: {}",
                    getNotificationChannel(), notificationId, e.getMessage(), e);

            try {
                // Try to update the notification record
                Notification notification = notificationRepository.findById(notificationId).orElse(null);
                if (notification != null) {
                    retryHandler.handleFailure(
                            notification,
                            notificationMessage,
                            e.getMessage(),
                            "PROCESSING_ERROR",
                            com.example.notification.model.enums.ErrorType.RETRIABLE
                    );
                }

                // Acknowledge to prevent infinite redelivery
                acknowledgeMessage(channel, message);

            } catch (Exception ex) {
                log.error("Failed to handle error for notification {}: {}",
                        notificationId, ex.getMessage());
                rejectMessage(channel, message);
            }
        }
    }

    /**
     * Get the notification channel this worker handles.
     */
    protected abstract NotificationChannel getNotificationChannel();

    /**
     * Get the provider for this channel.
     */
    protected abstract NotificationProvider getProvider();

    /**
     * Acknowledge a message.
     */
    protected void acknowledgeMessage(Channel channel, Message message) {
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("Failed to acknowledge message: {}", e.getMessage());
        }
    }

    /**
     * Reject a message (send to DLQ).
     */
    protected void rejectMessage(Channel channel, Message message) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException e) {
            log.error("Failed to reject message: {}", e.getMessage());
        }
    }
}
