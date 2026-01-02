package com.example.security.jwt;

import com.example.config.AppProperties;
import com.example.security.exception.JwtTokenException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtSignKeyProvider jwtSignKeyProvider;
    private final AppProperties appProperties;

    public String generateToken(final Map<String, Object> extraClaims,
                                final String email) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plusMillis(appProperties.getJwt().getExpiration());
            return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(jwtSignKeyProvider.get())
                .compact();
        } catch (JwtException exception) {
            log.error("JWT token creation failed for user: {}", email, exception);
            throw new JwtTokenException(exception);
        }
    }

    public String generateRefreshToken(final UserDetails userDetails) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plusMillis(appProperties.getJwt().getRefreshExpiration());
            return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(jwtSignKeyProvider.getRefresh())
                .compact();
        } catch (JwtException exception) {
            log.error("JWT refresh token creation failed for user: {}", userDetails.getUsername(), exception);
            throw new JwtTokenException(exception);
        }
    }

    public long expiresInSeconds(){
        return appProperties.getJwt().getExpiration();
    }
}
