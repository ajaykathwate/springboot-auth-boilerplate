package com.example.notification.publisher;

import com.example.config.notification.NotificationProperties;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Abstract base class for channel publishers.
 * Provides common publishing logic to RabbitMQ.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractChannelPublisher implements ChannelPublisher {

    protected final RabbitTemplate rabbitTemplate;
    protected final NotificationProperties properties;

    @Override
    public void publish(NotificationMessage message) {
        String exchange = properties.getQueue().getExchange();
        String routingKey = getChannel().getRoutingKey();

        log.debug("Publishing {} notification to exchange: {}, routingKey: {}",
                getChannel(), exchange, routingKey);

        rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
            m.getMessageProperties().setHeader("x-channel", getChannel().name());
            m.getMessageProperties().setHeader("x-notification-id", message.getNotificationId());
            m.getMessageProperties().setHeader("x-user-id", message.getUserId());
            m.getMessageProperties().setPriority(message.getPriority());
            return m;
        });

        log.info("Published {} notification {} for user {} to queue",
                getChannel(), message.getNotificationId(), message.getUserId());
    }

    public abstract NotificationChannel getChannel();
}
