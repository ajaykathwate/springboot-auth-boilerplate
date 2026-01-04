package com.example.notification.service.impl;

import com.example.config.notification.NotificationProperties;
import com.example.notification.model.enums.NotificationChannel;
import com.example.notification.service.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiter implementation using token bucket algorithm.
 */
@Service
@Slf4j
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final String RATE_LIMIT_KEY_PREFIX = "notification:rate_limit:";

    private final StringRedisTemplate redisTemplate;
    private final NotificationProperties properties;

    public RateLimiterServiceImpl(
            @Qualifier("notificationRedisTemplate") StringRedisTemplate redisTemplate,
            NotificationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public boolean isAllowed(Long userId, NotificationChannel channel) {
        String key = buildKey(userId, channel);
        NotificationProperties.RateLimitConfig config = getRateLimitConfig(channel);

        String currentCountStr = redisTemplate.opsForValue().get(key);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;

        boolean allowed = currentCount < config.getMaxRequests();

        if (!allowed) {
            log.warn("Rate limit exceeded for user {} on channel {}. Current: {}, Max: {}",
                    userId, channel, currentCount, config.getMaxRequests());
        }

        return allowed;
    }

    @Override
    public void recordAttempt(Long userId, NotificationChannel channel) {
        String key = buildKey(userId, channel);
        NotificationProperties.RateLimitConfig config = getRateLimitConfig(channel);

        Long newCount = redisTemplate.opsForValue().increment(key);

        // Set expiration only if this is a new key (count == 1)
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(config.getWindowSeconds()));
        }

        log.debug("Recorded notification attempt for user {} on channel {}. Count: {}",
                userId, channel, newCount);
    }

    @Override
    public int getRemainingQuota(Long userId, NotificationChannel channel) {
        String key = buildKey(userId, channel);
        NotificationProperties.RateLimitConfig config = getRateLimitConfig(channel);

        String currentCountStr = redisTemplate.opsForValue().get(key);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;

        return Math.max(0, config.getMaxRequests() - currentCount);
    }

    @Override
    public long getTimeToReset(Long userId, NotificationChannel channel) {
        String key = buildKey(userId, channel);

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }

    /**
     * Build the Redis key for rate limiting.
     * Format: notification:rate_limit:{channel}:{userId}
     */
    private String buildKey(Long userId, NotificationChannel channel) {
        return RATE_LIMIT_KEY_PREFIX + channel.name().toLowerCase() + ":" + userId;
    }

    /**
     * Get rate limit configuration for a channel.
     * Falls back to default values if not configured.
     */
    private NotificationProperties.RateLimitConfig getRateLimitConfig(NotificationChannel channel) {
        NotificationProperties.RateLimitConfig config =
                properties.getRateLimit().get(channel.name().toLowerCase());

        if (config == null) {
            // Return default config
            config = new NotificationProperties.RateLimitConfig();
            switch (channel) {
                case EMAIL -> {
                    config.setMaxRequests(50);
                    config.setWindowSeconds(3600);
                }
                case SMS -> {
                    config.setMaxRequests(10);
                    config.setWindowSeconds(3600);
                }
                case WHATSAPP -> {
                    config.setMaxRequests(20);
                    config.setWindowSeconds(3600);
                }
                case PUSH -> {
                    config.setMaxRequests(100);
                    config.setWindowSeconds(3600);
                }
                case IN_APP -> {
                    config.setMaxRequests(200);
                    config.setWindowSeconds(3600);
                }
            }
        }

        return config;
    }
}
