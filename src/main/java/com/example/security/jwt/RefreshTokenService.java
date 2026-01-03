package com.example.security.jwt;

import com.example.config.AppProperties;
import com.example.security.exception.InvalidRefreshTokenException;
import io.jsonwebtoken.Jwts;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtSignKeyProvider jwtSignKeyProvider;
    private final AppProperties appProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh:";

    public void store(String refreshToken, String email) {
        redisTemplate.opsForValue().set(
            PREFIX + refreshToken,
            email,
            appProperties.getJwt().getRefreshExpiration(),
            TimeUnit.MILLISECONDS
        );
    }

    public String validateAndGetEmail(String refreshToken) {
        try {
            var claims = Jwts.parser()
                .verifyWith(jwtSignKeyProvider.getRefresh())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

            String email = claims.getSubject();

            String stored = redisTemplate.opsForValue().get(PREFIX + refreshToken);
            if (stored == null || !stored.equals(email)) {
                throw new InvalidRefreshTokenException("Refresh token invalid or expired");
            }

            return email;
        }catch (Exception ex) {
            throw new InvalidRefreshTokenException("Refresh token invalid or expired");
        }
    }

    public void invalidate(String refreshToken) {
        redisTemplate.delete(PREFIX + refreshToken);
    }
}

