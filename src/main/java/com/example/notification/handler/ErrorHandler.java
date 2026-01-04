package com.example.notification.handler;

import com.example.notification.model.enums.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Handler for classifying errors as retriable or permanent.
 */
@Component
@Slf4j
public class ErrorHandler {

    /**
     * Error codes that indicate permanent failures (should not retry).
     */
    private static final Set<String> PERMANENT_ERROR_CODES = Set.of(
            // Twilio error codes for permanent failures
            "21211",  // Invalid phone number
            "21612",  // Phone number not SMS capable
            "21614",  // Invalid mobile number
            "21408",  // Permission to send not enabled
            "21610",  // Unsubscribed recipient
            "30004",  // Message blocked
            "30005",  // Unknown destination
            "30006",  // Landline or unreachable
            "30007",  // Message filtered

            // Email permanent failures
            "550",    // Mailbox unavailable
            "551",    // User not local
            "552",    // Exceeded storage
            "553",    // Mailbox name not allowed
            "554",    // Transaction failed

            // Firebase permanent failures
            "INVALID_ARGUMENT",
            "NOT_FOUND",
            "UNREGISTERED",

            // Generic permanent error codes
            "INVALID_RECIPIENT",
            "BLOCKED",
            "UNSUBSCRIBED"
    );

    /**
     * Error messages that indicate permanent failures.
     */
    private static final Set<String> PERMANENT_ERROR_PATTERNS = Set.of(
            "invalid",
            "not found",
            "blocked",
            "unsubscribed",
            "blacklisted",
            "opt-out",
            "unregistered",
            "does not exist",
            "permission denied"
    );

    /**
     * Classify an error as retriable or permanent.
     *
     * @param exception the exception that occurred
     * @param errorCode the error code from the provider (if any)
     * @return the error type
     */
    public ErrorType classifyError(Exception exception, String errorCode) {
        // Check error code first
        if (errorCode != null && PERMANENT_ERROR_CODES.contains(errorCode.toUpperCase())) {
            log.debug("Classified as PERMANENT based on error code: {}", errorCode);
            return ErrorType.PERMANENT;
        }

        // Check exception type
        if (isRetriableException(exception)) {
            log.debug("Classified as RETRIABLE based on exception type: {}",
                    exception.getClass().getSimpleName());
            return ErrorType.RETRIABLE;
        }

        // Check error message patterns
        String errorMessage = exception.getMessage();
        if (errorMessage != null) {
            String lowerMessage = errorMessage.toLowerCase();
            for (String pattern : PERMANENT_ERROR_PATTERNS) {
                if (lowerMessage.contains(pattern)) {
                    log.debug("Classified as PERMANENT based on error message pattern: {}", pattern);
                    return ErrorType.PERMANENT;
                }
            }
        }

        // Default to retriable for unknown errors
        log.debug("Classified as RETRIABLE by default for error: {}", exception.getMessage());
        return ErrorType.RETRIABLE;
    }

    /**
     * Classify an error based on error code and message alone.
     *
     * @param errorCode    the error code from the provider
     * @param errorMessage the error message
     * @return the error type
     */
    public ErrorType classifyError(String errorCode, String errorMessage) {
        // Check error code
        if (errorCode != null && PERMANENT_ERROR_CODES.contains(errorCode.toUpperCase())) {
            return ErrorType.PERMANENT;
        }

        // Check error message patterns
        if (errorMessage != null) {
            String lowerMessage = errorMessage.toLowerCase();
            for (String pattern : PERMANENT_ERROR_PATTERNS) {
                if (lowerMessage.contains(pattern)) {
                    return ErrorType.PERMANENT;
                }
            }
        }

        return ErrorType.RETRIABLE;
    }

    /**
     * Check if an exception type indicates a retriable error.
     */
    private boolean isRetriableException(Exception exception) {
        if (exception == null) {
            return true;
        }

        // Network-related exceptions are usually retriable
        if (exception instanceof SocketTimeoutException ||
                exception instanceof UnknownHostException ||
                exception instanceof java.net.ConnectException ||
                exception instanceof java.io.IOException) {
            return true;
        }

        // Check for rate limiting exceptions
        String message = exception.getMessage();
        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("rate limit") ||
                    lower.contains("too many requests") ||
                    lower.contains("timeout") ||
                    lower.contains("connection") ||
                    lower.contains("temporarily unavailable")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extract error code from exception if available.
     */
    public String extractErrorCode(Exception exception) {
        // Could be enhanced to extract codes from specific exception types
        // For now, return null and let callers provide the code
        return null;
    }

    /**
     * Build a user-friendly error message.
     */
    public String buildErrorMessage(Exception exception, String providerResponse) {
        StringBuilder message = new StringBuilder();

        if (exception != null) {
            message.append(exception.getClass().getSimpleName())
                    .append(": ")
                    .append(exception.getMessage());
        }

        if (providerResponse != null && !providerResponse.isEmpty()) {
            if (message.length() > 0) {
                message.append(" | Provider response: ");
            }
            message.append(providerResponse);
        }

        return message.length() > 0 ? message.toString() : "Unknown error";
    }
}
