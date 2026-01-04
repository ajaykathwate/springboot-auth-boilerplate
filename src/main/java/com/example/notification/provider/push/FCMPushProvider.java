package com.example.notification.provider.push;

import com.example.config.notification.NotificationProperties;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.dto.ProviderResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Push notification provider implementation using Firebase Cloud Messaging.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FCMPushProvider implements PushProvider {

    private final NotificationProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public ProviderResponse send(NotificationMessage message) {
        if (!isEnabled()) {
            log.warn("Firebase Push is not enabled. Skipping push to token: {}",
                    maskToken(message.getRecipient()));
            return ProviderResponse.permanentFailure(
                    "Push provider is not enabled",
                    "PROVIDER_DISABLED",
                    null
            );
        }

        try {
            // Parse rendered content if it's JSON
            String title = message.getSubject() != null ? message.getSubject() : "Notification";
            String body = message.getRenderedContent();
            Map<String, String> data = new HashMap<>();

            // Try to parse JSON content for push
            try {
                JsonNode jsonNode = objectMapper.readTree(body);
                if (jsonNode.has("title")) {
                    title = jsonNode.get("title").asText();
                }
                if (jsonNode.has("body")) {
                    body = jsonNode.get("body").asText();
                }
                if (jsonNode.has("data") && jsonNode.get("data").isObject()) {
                    jsonNode.get("data").fields().forEachRemaining(entry ->
                            data.put(entry.getKey(), entry.getValue().asText()));
                }
            } catch (JsonProcessingException e) {
                // Not JSON, use content as-is
                log.debug("Push content is not JSON, using as plain body");
            }

            // Build FCM message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(message.getRecipient())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // Add data payload if present
            if (!data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // Add custom metadata
            if (message.getTemplateData() != null) {
                messageBuilder.putData("notificationId", String.valueOf(message.getNotificationId()));
                messageBuilder.putData("templateCode", message.getTemplateCode());
            }

            // Send the message
            String messageId = FirebaseMessaging.getInstance().send(messageBuilder.build());

            log.info("Push notification sent successfully. MessageId: {}, Token: {}",
                    messageId, maskToken(message.getRecipient()));

            return ProviderResponse.success(
                    messageId,
                    String.format("{\"messageId\":\"%s\"}", messageId)
            );

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification. Error code: {}, Message: {}",
                    e.getMessagingErrorCode(), e.getMessage());

            String errorCode = e.getMessagingErrorCode() != null
                    ? e.getMessagingErrorCode().name()
                    : "UNKNOWN";

            if (isPermanentError(e.getMessagingErrorCode())) {
                return ProviderResponse.permanentFailure(
                        e.getMessage(),
                        errorCode,
                        String.format("{\"errorCode\":\"%s\",\"message\":\"%s\"}", errorCode, e.getMessage())
                );
            } else {
                return ProviderResponse.retriableFailure(
                        e.getMessage(),
                        errorCode,
                        String.format("{\"errorCode\":\"%s\",\"message\":\"%s\"}", errorCode, e.getMessage())
                );
            }

        } catch (Exception e) {
            log.error("Unexpected error sending push notification. Error: {}", e.getMessage());

            return ProviderResponse.retriableFailure(
                    e.getMessage(),
                    "UNKNOWN_ERROR",
                    e.getClass().getSimpleName()
            );
        }
    }

    @Override
    public boolean isEnabled() {
        return properties.getFirebase().isEnabled()
                && !FirebaseApp.getApps().isEmpty();
    }

    @Override
    public String getProviderName() {
        return "FCM";
    }

    /**
     * Check if FCM error code indicates permanent failure.
     */
    private boolean isPermanentError(MessagingErrorCode errorCode) {
        if (errorCode == null) return false;

        return switch (errorCode) {
            case INVALID_ARGUMENT,  // Invalid request
                 UNREGISTERED,      // Token no longer valid
                 SENDER_ID_MISMATCH // Wrong sender
                    -> true;
            default -> false;
        };
    }

    /**
     * Mask FCM token for logging (privacy).
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 5);
    }
}
