package com.example.security.google;

import com.example.security.dto.JwtResponse;
import com.example.security.exception.InvalidGoogleTokenException;
import com.example.security.jwt.TokenService;
import com.example.user.NameParts;
import com.example.user.NameUtils;
import com.example.user.User;
import com.example.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserService userService;
    private final TokenService tokenService;

    public JwtResponse loginWithGoogle(String idToken) {

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();
        String googleUserId = payload.getSubject();

        if (email == null || !emailVerified) {
            throw new InvalidGoogleTokenException("Google email is not verified");
        }

        if (googleUserId == null) {
            throw new InvalidGoogleTokenException("Invalid Google user identifier");
        }

        String fullName = payload.containsKey("name")
            ? String.valueOf(payload.get("name"))
            : email.substring(0, email.indexOf('@'));

        NameParts nameParts = NameUtils.extractNameParts(fullName, email);

        User user = userService.findOrCreateGoogleUser(
            email, nameParts.firstName(), nameParts.lastName(), googleUserId
        );

        return tokenService.issueTokens(user);
    }
}
