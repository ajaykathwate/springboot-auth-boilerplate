package com.example.security.jwt;

import com.example.config.AppProperties;
import com.example.security.exception.JwtTokenHasNoUserEmailException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtClaimExtractor {

    private final JwtSignKeyProvider jwtSignKeyProvider;

    public String extractEmail(final String jwtToken) {
        try {
            return Optional.ofNullable(extractAllClaims(jwtToken).getSubject())
                .filter(StringUtils::hasText)
                .filter(this::isValidEmailFormat)
                .orElseThrow(() -> new JwtTokenHasNoUserEmailException("Invalid or missing email in JWT token"));
        } catch (JwtTokenHasNoUserEmailException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtTokenHasNoUserEmailException("Failed to extract email from JWT token");
        }
    }

    public Long extractUserId(final String jwtToken) {
        try {
            return Optional.ofNullable(extractAllClaims(jwtToken).get("userId", Long.class))
                .filter(id -> id > 0)
                .orElseThrow(() -> new JwtTokenHasNoUserEmailException("Invalid or missing userId in JWT token"));
        } catch (JwtTokenHasNoUserEmailException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtTokenHasNoUserEmailException("Failed to extract userId from JWT token");
        }
    }

    public String extractRole(final String jwtToken) {
        try {
            return Optional.ofNullable(extractAllClaims(jwtToken).get("role", String.class))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new JwtTokenHasNoUserEmailException("Invalid or missing role in JWT token"));
        } catch (JwtTokenHasNoUserEmailException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtTokenHasNoUserEmailException("Failed to extract role from JWT token");
        }
    }

    public LocalDateTime extractExpiration(final String jwtToken) {
        try {
            Date expiration = extractAllClaims(jwtToken).getExpiration();
            if (expiration == null) {
                throw new IllegalArgumentException("JWT token has no expiration date");
            }
            return expiration.toInstant()
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to extract expiration from JWT token", ex);
        }
    }

    public boolean isTokenExpired(final String jwtToken) {
        try {
            LocalDateTime expiration = extractExpiration(jwtToken);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            return expiration.isBefore(now);
        } catch (Exception ex) {
            // If we can't determine expiration, treat as expired for security
            return true;
        }
    }

    private boolean isValidEmailFormat(String email) {
        return email != null &&
            email.contains("@") &&
            email.length() >= 5 &&
            email.length() <= 254 &&
            !email.startsWith("@") &&
            !email.endsWith("@");
    }

    private Claims extractAllClaims(final String jwtToken) {
        return Jwts.parser()
            .verifyWith(jwtSignKeyProvider.get())
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload();
    }
}
