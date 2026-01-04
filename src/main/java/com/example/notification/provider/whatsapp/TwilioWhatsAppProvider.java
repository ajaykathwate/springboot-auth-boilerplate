package com.example.notification.provider.whatsapp;

import com.example.config.notification.NotificationProperties;
import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.dto.ProviderResponse;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WhatsApp provider implementation using Twilio WhatsApp API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TwilioWhatsAppProvider implements WhatsAppProvider {

    private static final String WHATSAPP_PREFIX = "whatsapp:";

    private final NotificationProperties properties;

    @Override
    public ProviderResponse send(NotificationMessage message) {
        if (!isEnabled()) {
            log.warn("Twilio WhatsApp is not enabled. Skipping message to: {}", message.getRecipient());
            return ProviderResponse.permanentFailure(
                    "WhatsApp provider is not enabled",
                    "PROVIDER_DISABLED",
                    null
            );
        }

        try {
            // Format phone numbers for WhatsApp
            String toNumber = formatWhatsAppNumber(message.getRecipient());
            String fromNumber = properties.getTwilio().getWhatsappNumber();

            // Ensure from number has whatsapp: prefix
            if (!fromNumber.startsWith(WHATSAPP_PREFIX)) {
                fromNumber = WHATSAPP_PREFIX + fromNumber;
            }

            Message twilioMessage = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    message.getRenderedContent()
            ).create();

            String messageId = twilioMessage.getSid();
            String status = twilioMessage.getStatus().toString();

            log.info("WhatsApp message sent successfully to: {}. SID: {}, Status: {}",
                    message.getRecipient(), messageId, status);

            return ProviderResponse.success(
                    messageId,
                    String.format("{\"sid\":\"%s\",\"status\":\"%s\"}", messageId, status)
            );

        } catch (ApiException e) {
            log.error("Failed to send WhatsApp message to: {}. Error code: {}, Message: {}",
                    message.getRecipient(), e.getCode(), e.getMessage());

            String errorCode = String.valueOf(e.getCode());

            if (isPermanentError(e.getCode())) {
                return ProviderResponse.permanentFailure(
                        e.getMessage(),
                        errorCode,
                        String.format("{\"code\":%d,\"message\":\"%s\"}", e.getCode(), e.getMessage())
                );
            } else {
                return ProviderResponse.retriableFailure(
                        e.getMessage(),
                        errorCode,
                        String.format("{\"code\":%d,\"message\":\"%s\"}", e.getCode(), e.getMessage())
                );
            }

        } catch (Exception e) {
            log.error("Unexpected error sending WhatsApp message to: {}. Error: {}",
                    message.getRecipient(), e.getMessage());

            return ProviderResponse.retriableFailure(
                    e.getMessage(),
                    "UNKNOWN_ERROR",
                    e.getClass().getSimpleName()
            );
        }
    }

    @Override
    public boolean isEnabled() {
        return properties.getTwilio().isEnabled()
                && properties.getTwilio().getAccountSid() != null
                && properties.getTwilio().getAuthToken() != null
                && properties.getTwilio().getWhatsappNumber() != null;
    }

    @Override
    public String getProviderName() {
        return "TwilioWhatsApp";
    }

    /**
     * Format phone number for WhatsApp API.
     */
    private String formatWhatsAppNumber(String phoneNumber) {
        if (phoneNumber.startsWith(WHATSAPP_PREFIX)) {
            return phoneNumber;
        }
        return WHATSAPP_PREFIX + phoneNumber;
    }

    /**
     * Check if Twilio error code indicates permanent failure.
     */
    private boolean isPermanentError(int errorCode) {
        return switch (errorCode) {
            case 21211, // Invalid phone number
                 21408, // Permission not enabled
                 21610, // Unsubscribed
                 63003, // Channel not found
                 63007, // User not opted in
                 63016  // Template not approved
                    -> true;
            default -> false;
        };
    }
}
