package com.example.notification.worker;

import com.example.notification.handler.RetryHandler;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.provider.NotificationProvider;
import com.example.notification.provider.push.PushProvider;
import com.example.notification.repository.NotificationRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Worker for processing PUSH notifications.
 */
@Component
@Slf4j
public class PushWorker extends NotificationWorker {

    private final PushProvider pushProvider;

    public PushWorker(
            NotificationRepository notificationRepository,
            RetryHandler retryHandler,
            PushProvider pushProvider) {
        super(notificationRepository, retryHandler);
        this.pushProvider = pushProvider;
    }

    @RabbitListener(queues = "notification.push.queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(NotificationMessage notificationMessage, Message message, Channel channel) {
        log.debug("Received PUSH notification message: {}", notificationMessage.getNotificationId());
        processMessage(notificationMessage, message, channel);
    }

    @Override
    protected NotificationChannel getNotificationChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    protected NotificationProvider getProvider() {
        return pushProvider;
    }
}
