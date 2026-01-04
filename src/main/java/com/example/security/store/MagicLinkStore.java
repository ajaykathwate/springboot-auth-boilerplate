package com.example.security.store;

import com.example.config.AppProperties;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class MagicLinkStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    private static final String PREFIX = "magic:link:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    public String generateAndStore(String email) {
        String token = generateToken();

        String key = PREFIX + token;
        long ttlMinutes = appProperties.getMagicLink().getExpirationMinutes();

        redisTemplate.opsForValue().set(key, email.toLowerCase(), ttlMinutes, TimeUnit.MINUTES);

        log.debug("Magic link token stored for email: {}, TTL: {} minutes", email, ttlMinutes);

        return token;
    }

    public String getEmailByToken(String token) {
        String key = PREFIX + token;
        String email = redisTemplate.opsForValue().get(key);

        if (email == null) {
            log.debug("No magic link found for token");
            return null;
        }

        log.debug("Magic link token found for email: {}", email);
        return email;
    }

    public void delete(String token) {
        String key = PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Magic link token deleted");
    }

    public boolean exists(String token) {
        String key = PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public String buildMagicLinkUrl(String token) {
        return UriComponentsBuilder
            .fromHttpUrl(appProperties.getFrontend().getBaseUrl())
            .path(appProperties.getMagicLink().getEndpoint())
            .queryParam("token", token)
            .build()
            .toUriString();
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
