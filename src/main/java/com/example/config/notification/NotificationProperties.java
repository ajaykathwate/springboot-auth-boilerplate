package com.example.config.notification;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the notification service.
 */
@Configuration
@ConfigurationProperties(prefix = "notification")
@Validated
@Getter
@Setter
public class NotificationProperties {

    /**
     * Rate limiting configuration per channel
     */
    private Map<String, RateLimitConfig> rateLimit = new HashMap<>();

    /**
     * Retry configuration
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * RabbitMQ queue configuration
     */
    private QueueConfig queue = new QueueConfig();

    /**
     * Twilio provider configuration
     */
    private TwilioConfig twilio = new TwilioConfig();

    /**
     * Firebase configuration
     */
    private FirebaseConfig firebase = new FirebaseConfig();

    /**
     * Template configuration
     */
    private TemplateConfig template = new TemplateConfig();

    @Getter
    @Setter
    public static class RateLimitConfig {
        /**
         * Maximum requests allowed in the time window
         */
        private int maxRequests = 50;

        /**
         * Time window in seconds
         */
        private int windowSeconds = 3600;
    }

    @Getter
    @Setter
    public static class RetryConfig {
        /**
         * Maximum number of retry attempts
         */
        private int maxAttempts = 10;

        /**
         * Initial backoff delay in milliseconds
         */
        private long initialBackoffMs = 1000;

        /**
         * Backoff multiplier for exponential backoff
         */
        private double multiplier = 2.0;

        /**
         * Maximum backoff delay in milliseconds
         */
        private long maxBackoffMs = 3600000; // 1 hour
    }

    @Getter
    @Setter
    public static class QueueConfig {
        /**
         * Exchange name for notifications
         */
        private String exchange = "notification.exchange";

        /**
         * Dead letter exchange name
         */
        private String dlxExchange = "notification.dlx";

        /**
         * Dead letter queue name
         */
        private String dlqQueue = "notification.dlq";

        /**
         * Prefetch count for workers
         */
        private int prefetchCount = 1;
    }

    @Getter
    @Setter
    public static class TwilioConfig {
        /**
         * Twilio Account SID
         */
        private String accountSid;

        /**
         * Twilio Auth Token
         */
        private String authToken;

        /**
         * Twilio phone number for SMS
         */
        private String fromNumber;

        /**
         * Twilio WhatsApp number (with whatsapp: prefix)
         */
        private String whatsappNumber;

        /**
         * Whether Twilio is enabled
         */
        private boolean enabled = false;
    }

    @Getter
    @Setter
    public static class FirebaseConfig {
        /**
         * Path to Firebase service account JSON file
         */
        private String serviceAccountPath;

        /**
         * Whether Firebase is enabled
         */
        private boolean enabled = false;
    }

    @Getter
    @Setter
    public static class TemplateConfig {
        /**
         * Base path for templates
         */
        private String basePath = "classpath:/templates/notifications/";

        /**
         * Whether to cache templates
         */
        private boolean cacheEnabled = true;
    }
}
