package com.example.security.emailotp;

import com.example.config.AppProperties;
import com.example.notification.service.NotificationService;
import com.example.security.dto.JwtResponse;
import com.example.security.exception.InvalidOtpException;
import com.example.security.jwt.TokenService;
import com.example.security.store.OtpStore;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOtpAuthService {

    private final OtpStore otpStore;
    private final NotificationService notificationService;
    private final UserService userService;
    private final TokenService tokenService;
    private final AppProperties appProperties;

    private static final String OTP_TEMPLATE_CODE = "otp-verification";

    public void sendOtp(String email) {
        String otp = otpStore.generateAndStore(email);

        Map<String, Object> templateData = Map.of(
            "otp", otp,
            "expirationMinutes", 5
        );

        // Create a new not verified user if new user
        User user = userService.findOrCreateEmailOtpUser(email);

        try {
            notificationService.sendEmail(
                user.getId(),
                email,
                OTP_TEMPLATE_CODE,
                "Here is your email verification code",
                templateData
            );
        } catch (Exception ex) {
            log.warn("Failed to send welcome email for userId={}, email={}", user.getId(), email, ex);
        }

        log.info("OTP sent to email: {}", email);
    }

    public JwtResponse verifyOtp(String email, String otp) {
        if (!otpStore.exists(email)) {
            throw new InvalidOtpException("OTP has expired or does not exist. Please request a new one.");
        }

        if (!otpStore.verify(email, otp)) {
            throw new InvalidOtpException("Invalid OTP. Please check and try again.");
        }

        otpStore.delete(email);

        // mark that user as Verified
        userService.markUserAsVerified(email);

        // get user by email
        User user = userService.getByEmail(email);

        log.info("OTP verified successfully for email: {}", email);

        return tokenService.issueTokens(user);
    }
}
