package com.example.notification.worker;

import com.example.notification.handler.RetryHandler;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.provider.NotificationProvider;
import com.example.notification.provider.whatsapp.WhatsAppProvider;
import com.example.notification.repository.NotificationRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Worker for processing WHATSAPP notifications.
 */
@Component
@Slf4j
public class WhatsAppWorker extends NotificationWorker {

    private final WhatsAppProvider whatsAppProvider;

    public WhatsAppWorker(
            NotificationRepository notificationRepository,
            RetryHandler retryHandler,
            WhatsAppProvider whatsAppProvider) {
        super(notificationRepository, retryHandler);
        this.whatsAppProvider = whatsAppProvider;
    }

    @RabbitListener(queues = "notification.whatsapp.queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(NotificationMessage notificationMessage, Message message, Channel channel) {
        log.debug("Received WHATSAPP notification message: {}", notificationMessage.getNotificationId());
        processMessage(notificationMessage, message, channel);
    }

    @Override
    protected NotificationChannel getNotificationChannel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    protected NotificationProvider getProvider() {
        return whatsAppProvider;
    }
}
