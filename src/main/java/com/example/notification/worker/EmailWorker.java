package com.example.notification.worker;

import com.example.notification.handler.RetryHandler;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.provider.NotificationProvider;
import com.example.notification.provider.email.EmailProvider;
import com.example.notification.repository.NotificationRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Worker for processing EMAIL notifications.
 */
@Component
@Slf4j
public class EmailWorker extends NotificationWorker {

    private final EmailProvider emailProvider;

    public EmailWorker(
            NotificationRepository notificationRepository,
            RetryHandler retryHandler,
            EmailProvider emailProvider) {
        super(notificationRepository, retryHandler);
        this.emailProvider = emailProvider;
    }

    @RabbitListener(queues = "notification.email.queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(NotificationMessage notificationMessage, Message message, Channel channel) {
        log.debug("Received EMAIL notification message: {}", notificationMessage.getNotificationId());
        processMessage(notificationMessage, message, channel);
    }

    @Override
    protected NotificationChannel getNotificationChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    protected NotificationProvider getProvider() {
        return emailProvider;
    }
}
