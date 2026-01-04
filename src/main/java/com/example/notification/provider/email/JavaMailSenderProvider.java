package com.example.notification.provider.email;

import com.example.notification.model.dto.NotificationMessage;
import com.example.notification.model.dto.ProviderResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Email provider implementation using Spring's JavaMailSender.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JavaMailSenderProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Override
    public ProviderResponse send(NotificationMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(message.getRecipient());
            helper.setSubject(message.getSubject() != null ? message.getSubject() : "Notification");
            helper.setText(message.getRenderedContent(), true); // true = HTML content

            mailSender.send(mimeMessage);

            // Generate a unique message ID for tracking
            String messageId = UUID.randomUUID().toString();

            log.info("Email sent successfully to: {}. MessageId: {}", message.getRecipient(), messageId);

            return ProviderResponse.success(messageId, "Email sent successfully");

        } catch (MailException e) {
            log.error("Failed to send email to: {}. Error: {}", message.getRecipient(), e.getMessage());

            // Classify error based on exception message
            if (isRetriableError(e)) {
                return ProviderResponse.retriableFailure(
                        e.getMessage(),
                        extractErrorCode(e),
                        e.getClass().getSimpleName()
                );
            } else {
                return ProviderResponse.permanentFailure(
                        e.getMessage(),
                        extractErrorCode(e),
                        e.getClass().getSimpleName()
                );
            }

        } catch (MessagingException e) {
            log.error("Failed to create email message for: {}. Error: {}", message.getRecipient(), e.getMessage());
            return ProviderResponse.permanentFailure(
                    e.getMessage(),
                    "MESSAGING_ERROR",
                    e.getClass().getSimpleName()
            );
        }
    }

    @Override
    public boolean isEnabled() {
        return mailSender != null;
    }

    @Override
    public String getProviderName() {
        return "JavaMailSender";
    }

    private boolean isRetriableError(MailException e) {
        String message = e.getMessage();
        if (message == null) return true;

        String lower = message.toLowerCase();
        return lower.contains("connection") ||
                lower.contains("timeout") ||
                lower.contains("temporarily") ||
                lower.contains("try again");
    }

    private String extractErrorCode(MailException e) {
        String message = e.getMessage();
        if (message != null) {
            // Try to extract SMTP error code (e.g., 550, 553)
            if (message.matches(".*\\b(5\\d{2})\\b.*")) {
                return message.replaceAll(".*\\b(5\\d{2})\\b.*", "$1");
            }
        }
        return "MAIL_ERROR";
    }
}
