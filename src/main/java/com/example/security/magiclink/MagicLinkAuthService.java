package com.example.security.magiclink;

import com.example.notification.service.NotificationService;
import com.example.security.dto.JwtResponse;
import com.example.security.exception.InvalidMagicLinkTokenException;
import com.example.security.jwt.TokenService;
import com.example.security.store.MagicLinkStore;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagicLinkAuthService {

    private final MagicLinkStore magicLinkStore;
    private final NotificationService notificationService;
    private final UserService userService;
    private final TokenService tokenService;

    private static final String MAGIC_LINK_TEMPLATE_CODE = "magic-link";

    public void sendMagicLink(String email) {
        String token = magicLinkStore.generateAndStore(email);
        String magicLinkUrl = magicLinkStore.buildMagicLinkUrl(token);

        Map<String, Object> templateData = Map.of(
            "magicLink", magicLinkUrl,
            "expirationMinutes", 10
        );

        // If new user - create unverified user
        User user = userService.findOrCreateMagicLinkUser(email);

        notificationService.sendEmail(
            user.getId(),
            email,
            MAGIC_LINK_TEMPLATE_CODE,
            "Sign in to your account",
            templateData
        );

        log.info("Magic link sent to email: {}", email);
    }

    public JwtResponse verifyMagicLink(String token) {
        String email = magicLinkStore.getEmailByToken(token);

        if (email == null) {
            throw new InvalidMagicLinkTokenException(
                "Magic link has expired or is invalid. Please request a new one."
            );
        }

        magicLinkStore.delete(token);

        // mark user as verified
        userService.markUserAsVerified(email);

        User user = userService.getByEmail(email);

        log.info("Magic link verified successfully for email: {}", email);

        return tokenService.issueTokens(user);
    }
}
