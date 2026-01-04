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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Worker for processing IN_APP notifications.
 * IN_APP notifications don't need an external provider - they're stored in the database.
 */
@Component
@Slf4j
public class InAppWorker extends NotificationWorker {

    public InAppWorker(NotificationRepository notificationRepository, RetryHandler retryHandler) {
        super(notificationRepository, retryHandler);
    }

    @RabbitListener(queues = "notification.inapp.queue", containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void onMessage(NotificationMessage notificationMessage, Message message, Channel channel) {
        log.debug("Received IN_APP notification message: {}", notificationMessage.getNotificationId());

        Long notificationId = notificationMessage.getNotificationId();

        try {
            // Fetch notification from database
            Notification notification = notificationRepository.findById(notificationId).orElse(null);

            if (notification == null) {
                log.error("Notification {} not found in database. Acknowledging message.", notificationId);
                acknowledgeMessage(channel, message);
                return;
            }

            // For IN_APP, we just mark it as delivered
            // The content is already stored in the notification record
            notification.setStatus(NotificationStatus.DELIVERED);
            notification.setDeliveredAt(java.time.LocalDateTime.now());
            notification.setSentAt(java.time.LocalDateTime.now());
            notification.setExternalId(UUID.randomUUID().toString()); // Generate internal ID
            notification.setProviderResponse("{\"status\":\"stored\"}");

            notificationRepository.save(notification);

            log.info("IN_APP notification {} delivered (stored in database)", notificationId);

            acknowledgeMessage(channel, message);

        } catch (Exception e) {
            log.error("Error processing IN_APP notification {}: {}",
                    notificationId, e.getMessage(), e);

            try {
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
                acknowledgeMessage(channel, message);
            } catch (Exception ex) {
                log.error("Failed to handle error for IN_APP notification {}: {}",
                        notificationId, ex.getMessage());
                rejectMessage(channel, message);
            }
        }
    }

    @Override
    protected NotificationChannel getNotificationChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    protected NotificationProvider getProvider() {
        // IN_APP doesn't use an external provider
        return new InAppProvider();
    }

    /**
     * Internal provider for IN_APP notifications (no external calls needed).
     */
    private static class InAppProvider implements NotificationProvider {
        @Override
        public ProviderResponse send(NotificationMessage message) {
            // IN_APP just stores in database, no external send needed
            return ProviderResponse.success(
                    UUID.randomUUID().toString(),
                    "{\"status\":\"stored\"}"
            );
        }

        @Override
        public boolean isEnabled() {
            return true; // Always enabled
        }

        @Override
        public String getProviderName() {
            return "InApp";
        }
    }
}
