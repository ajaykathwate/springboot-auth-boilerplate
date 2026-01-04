package com.example.notification.model.dto;

import com.example.notification.model.enums.ErrorType;
import lombok.*;

/**
 * Standardized response from notification providers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResponse {

    /**
     * Whether the send was successful
     */
    private boolean success;

    /**
     * External message ID from provider (for tracking)
     */
    private String messageId;

    /**
     * Error message if failed
     */
    private String errorMessage;

    /**
     * Error code from provider
     */
    private String errorCode;

    /**
     * Type of error (retriable or permanent)
     */
    private ErrorType errorType;

    /**
     * Raw response from provider as JSON
     */
    private String rawResponse;

    /**
     * Create a success response
     */
    public static ProviderResponse success(String messageId, String rawResponse) {
        return ProviderResponse.builder()
                .success(true)
                .messageId(messageId)
                .rawResponse(rawResponse)
                .build();
    }

    /**
     * Create a retriable failure response
     */
    public static ProviderResponse retriableFailure(String errorMessage, String errorCode, String rawResponse) {
        return ProviderResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .errorType(ErrorType.RETRIABLE)
                .rawResponse(rawResponse)
                .build();
    }

    /**
     * Create a permanent failure response
     */
    public static ProviderResponse permanentFailure(String errorMessage, String errorCode, String rawResponse) {
        return ProviderResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .errorType(ErrorType.PERMANENT)
                .rawResponse(rawResponse)
                .build();
    }
}
