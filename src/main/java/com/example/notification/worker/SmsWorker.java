package com.example.notification.worker;

import com.example.notification.handler.RetryHandler;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.provider.NotificationProvider;
import com.example.notification.provider.sms.SmsProvider;
import com.example.notification.repository.NotificationRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Worker for processing SMS notifications.
 */
@Component
@Slf4j
public class SmsWorker extends NotificationWorker {

    private final SmsProvider smsProvider;

    public SmsWorker(
            NotificationRepository notificationRepository,
            RetryHandler retryHandler,
            SmsProvider smsProvider) {
        super(notificationRepository, retryHandler);
        this.smsProvider = smsProvider;
    }

    @RabbitListener(queues = "notification.sms.queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(NotificationMessage notificationMessage, Message message, Channel channel) {
        log.debug("Received SMS notification message: {}", notificationMessage.getNotificationId());
        processMessage(notificationMessage, message, channel);
    }

    @Override
    protected NotificationChannel getNotificationChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    protected NotificationProvider getProvider() {
        return smsProvider;
    }
}
