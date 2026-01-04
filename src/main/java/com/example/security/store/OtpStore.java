package com.example.security.store;

import com.example.config.AppProperties;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    private static final String PREFIX = "email:otp:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateAndStore(String email) {
        String otp = generateOtp();
        String hashedOtp = hashOtp(otp);

        String key = PREFIX + email.toLowerCase();
        long ttlMinutes = appProperties.getOtp().getExpirationMinutes();

        redisTemplate.opsForValue().set(key, hashedOtp, ttlMinutes, TimeUnit.MINUTES);

        log.debug("OTP stored for email: {}, TTL: {} minutes", email, ttlMinutes);

        return otp;
    }

    public boolean verify(String email, String otp) {
        String key = PREFIX + email.toLowerCase();
        String storedHash = redisTemplate.opsForValue().get(key);

        if (storedHash == null) {
            log.debug("No OTP found for email: {}", email);
            return false;
        }

        String providedHash = hashOtp(otp);
        boolean matches = storedHash.equals(providedHash);

        if (matches) {
            log.debug("OTP verified successfully for email: {}", email);
        } else {
            log.debug("OTP verification failed for email: {}", email);
        }

        return matches;
    }

    public void delete(String email) {
        String key = PREFIX + email.toLowerCase();
        redisTemplate.delete(key);
        log.debug("OTP deleted for email: {}", email);
    }

    public boolean exists(String email) {
        String key = PREFIX + email.toLowerCase();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String generateOtp() {
        int length = appProperties.getOtp().getLength();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(SECURE_RANDOM.nextInt(10));
        }

        return otp.toString();
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash OTP", e);
        }
    }
}
