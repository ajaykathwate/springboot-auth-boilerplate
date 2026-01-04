package com.example.notification.model.dto;

import lombok.*;

/**
 * Recipient contact details for multi-channel notifications.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientDetails {

    /**
     * Email address for EMAIL channel
     */
    private String email;

    /**
     * Phone number for SMS channel (E.164 format preferred: +1234567890)
     */
    private String phone;

    /**
     * WhatsApp number (E.164 format: +1234567890)
     */
    private String whatsappNumber;

    /**
     * Firebase Cloud Messaging token for PUSH channel
     */
    private String fcmToken;

    /**
     * Device token for push notifications (alternative to fcmToken)
     */
    private String deviceToken;

    /**
     * Get the appropriate recipient for a specific channel
     */
    public String getRecipientForChannel(String channel) {
        return switch (channel.toUpperCase()) {
            case "EMAIL" -> email;
            case "SMS" -> phone;
            case "WHATSAPP" -> whatsappNumber != null ? whatsappNumber : phone;
            case "PUSH" -> fcmToken != null ? fcmToken : deviceToken;
            case "IN_APP" -> null; // IN_APP doesn't need external recipient
            default -> null;
        };
    }
}
