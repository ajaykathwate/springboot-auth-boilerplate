package com.example.notification.provider.sms;

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
 * SMS provider implementation using Twilio API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsProvider implements SmsProvider {

    private final NotificationProperties properties;

    @Override
    public ProviderResponse send(NotificationMessage message) {
        if (!isEnabled()) {
            log.warn("Twilio SMS is not enabled. Skipping SMS to: {}", message.getRecipient());
            return ProviderResponse.permanentFailure(
                    "SMS provider is not enabled",
                    "PROVIDER_DISABLED",
                    null
            );
        }

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(message.getRecipient()),
                    new PhoneNumber(properties.getTwilio().getFromNumber()),
                    message.getRenderedContent()
            ).create();

            String messageId = twilioMessage.getSid();
            String status = twilioMessage.getStatus().toString();

            log.info("SMS sent successfully to: {}. SID: {}, Status: {}",
                    message.getRecipient(), messageId, status);

            return ProviderResponse.success(
                    messageId,
                    String.format("{\"sid\":\"%s\",\"status\":\"%s\"}", messageId, status)
            );

        } catch (ApiException e) {
            log.error("Failed to send SMS to: {}. Error code: {}, Message: {}",
                    message.getRecipient(), e.getCode(), e.getMessage());

            String errorCode = String.valueOf(e.getCode());

            // Classify error based on Twilio error code
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
            log.error("Unexpected error sending SMS to: {}. Error: {}",
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
                && properties.getTwilio().getFromNumber() != null;
    }

    @Override
    public String getProviderName() {
        return "TwilioSMS";
    }

    /**
     * Check if Twilio error code indicates permanent failure.
     */
    private boolean isPermanentError(int errorCode) {
        return switch (errorCode) {
            case 21211, // Invalid phone number
                 21612, // Phone not SMS capable
                 21614, // Invalid mobile number
                 21408, // Permission not enabled
                 21610, // Unsubscribed
                 30004, // Message blocked
                 30005, // Unknown destination
                 30006, // Landline
                 30007  // Filtered
                    -> true;
            default -> false;
        };
    }
}
