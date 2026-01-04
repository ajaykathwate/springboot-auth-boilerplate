package com.example.config.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration for external notification providers (Twilio, Firebase).
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class NotificationProviderConfig {

    private final NotificationProperties properties;

    /**
     * Initialize Twilio SDK with credentials.
     */
    @PostConstruct
    public void initializeTwilio() {
        if (properties.getTwilio().isEnabled()) {
            String accountSid = properties.getTwilio().getAccountSid();
            String authToken = properties.getTwilio().getAuthToken();

            if (accountSid != null && !accountSid.isBlank()
                    && authToken != null && !authToken.isBlank()) {
                Twilio.init(accountSid, authToken);
                log.info("Twilio SDK initialized successfully");
            } else {
                log.warn("Twilio is enabled but credentials are missing. SMS/WhatsApp notifications will fail.");
            }
        } else {
            log.info("Twilio is disabled. SMS/WhatsApp notifications will not be sent.");
        }
    }

    /**
     * Initialize Firebase Admin SDK with service account.
     */
    @PostConstruct
    public void initializeFirebase() {
        if (properties.getFirebase().isEnabled()) {
            String serviceAccountPath = properties.getFirebase().getServiceAccountPath();

            if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
                try {
                    if (FirebaseApp.getApps().isEmpty()) {
                        InputStream serviceAccount;

                        if (serviceAccountPath.startsWith("classpath:")) {
                            String path = serviceAccountPath.substring("classpath:".length());
                            serviceAccount = new ClassPathResource(path).getInputStream();
                        } else {
                            serviceAccount = new java.io.FileInputStream(serviceAccountPath);
                        }

                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();

                        FirebaseApp.initializeApp(options);
                        log.info("Firebase Admin SDK initialized successfully");
                    }
                } catch (IOException e) {
                    log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
                }
            } else {
                log.warn("Firebase is enabled but service account path is missing. Push notifications will fail.");
            }
        } else {
            log.info("Firebase is disabled. Push notifications will not be sent.");
        }
    }
}
