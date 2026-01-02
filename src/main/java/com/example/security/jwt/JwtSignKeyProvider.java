package com.example.security.jwt;

import com.example.config.AppProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtSignKeyProvider {
    private final AppProperties appProperties;

    public SecretKey get() {
        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public SecretKey getRefresh() {
        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getJwt().getRefreshSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
