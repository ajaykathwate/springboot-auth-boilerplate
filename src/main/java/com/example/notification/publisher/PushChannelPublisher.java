package com.example.notification.publisher;

import com.example.config.notification.NotificationProperties;
import com.example.notification.model.enums.NotificationChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for PUSH channel notifications.
 */
@Component
public class PushChannelPublisher extends AbstractChannelPublisher {

    public PushChannelPublisher(RabbitTemplate rabbitTemplate, NotificationProperties properties) {
        super(rabbitTemplate, properties);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }
}
