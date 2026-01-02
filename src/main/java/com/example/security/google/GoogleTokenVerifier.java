package com.example.security.google;

import com.example.config.AppProperties;
import com.example.security.exception.InvalidGoogleTokenException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(AppProperties appProperties) {
        try {
            String GOOGLE_CLIENT_ID = appProperties.getGoogle().getClientId();
            this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance()
            )
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to initialize GoogleTokenVerifier", e
            );
        }
    }

    public GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);

            if (token == null) {
                throw new InvalidGoogleTokenException("Invalid Google ID token");
            }

            return token.getPayload();

        } catch (InvalidGoogleTokenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidGoogleTokenException("Google token verification failed");
        }
    }
}


