package com.example.config.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis configuration specifically for notification rate limiting.
 * Uses the existing Redis connection factory from the application.
 */
@Configuration
public class NotificationRedisConfig {

    /**
     * StringRedisTemplate for rate limiting operations.
     * Uses string keys and values for simplicity in rate limit counting.
     */
    @Bean(name = "notificationRedisTemplate")
    public StringRedisTemplate notificationRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setEnableTransactionSupport(false);
        return template;
    }
}
