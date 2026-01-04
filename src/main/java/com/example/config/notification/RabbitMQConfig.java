package com.example.config.notification;

import com.example.notification.model.enums.NotificationChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ configuration for notification queues, exchanges, and bindings.
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final NotificationProperties properties;

    // ==================== Message Converter ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(properties.getQueue().getPrefetchCount());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // ==================== Exchanges ====================

    @Bean
    public DirectExchange notificationExchange() {
        return ExchangeBuilder
                .directExchange(properties.getQueue().getExchange())
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(properties.getQueue().getDlxExchange())
                .durable(true)
                .build();
    }

    // ==================== Dead Letter Queue ====================

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(properties.getQueue().getDlqQueue())
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("notification.dlq");
    }

    // ==================== Channel Queues ====================

    private Map<String, Object> getQueueArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getQueue().getDlxExchange());
        args.put("x-dead-letter-routing-key", "notification.dlq");
        return args;
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(NotificationChannel.EMAIL.getQueueName())
                .withArguments(getQueueArguments())
                .build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder
                .durable(NotificationChannel.SMS.getQueueName())
                .withArguments(getQueueArguments())
                .build();
    }

    @Bean
    public Queue whatsappQueue() {
        return QueueBuilder
                .durable(NotificationChannel.WHATSAPP.getQueueName())
                .withArguments(getQueueArguments())
                .build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder
                .durable(NotificationChannel.PUSH.getQueueName())
                .withArguments(getQueueArguments())
                .build();
    }

    @Bean
    public Queue inAppQueue() {
        return QueueBuilder
                .durable(NotificationChannel.IN_APP.getQueueName())
                .withArguments(getQueueArguments())
                .build();
    }

    // ==================== Bindings ====================

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(notificationExchange())
                .with(NotificationChannel.EMAIL.getRoutingKey());
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder
                .bind(smsQueue())
                .to(notificationExchange())
                .with(NotificationChannel.SMS.getRoutingKey());
    }

    @Bean
    public Binding whatsappBinding() {
        return BindingBuilder
                .bind(whatsappQueue())
                .to(notificationExchange())
                .with(NotificationChannel.WHATSAPP.getRoutingKey());
    }

    @Bean
    public Binding pushBinding() {
        return BindingBuilder
                .bind(pushQueue())
                .to(notificationExchange())
                .with(NotificationChannel.PUSH.getRoutingKey());
    }

    @Bean
    public Binding inAppBinding() {
        return BindingBuilder
                .bind(inAppQueue())
                .to(notificationExchange())
                .with(NotificationChannel.IN_APP.getRoutingKey());
    }
}
