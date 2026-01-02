package com.example.security.jwt;

import com.example.security.principal.SecurityUser;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

    private final JwtTokenFromAuthHeaderExtractor jwtTokenFromAuthHeaderExtractor;
    private final JwtClaimExtractor jwtClaimExtractor;

    public Authentication get(final HttpServletRequest httpRequest) {
        String jwtToken = jwtTokenFromAuthHeaderExtractor.extract(httpRequest);

        try {
            // Validate token expiration
            if (jwtClaimExtractor.isTokenExpired(jwtToken)) {
                throw new ExpiredJwtException(null, null, "JWT token has expired");
            }

            String userEmail = jwtClaimExtractor.extractEmail(jwtToken);
            Long userId = jwtClaimExtractor.extractUserId(jwtToken);
            String userRole = jwtClaimExtractor.extractRole(jwtToken);

            return getUsernamePasswordAuthenticationToken(userId, userEmail, userRole);

        } catch (Exception exception) {
            log.debug("Authentication failed: {}", exception.getMessage());
            throw exception;
        }
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(Long userId, String email, String role) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId for authentication");
        }

        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        SecurityUser securityUser = SecurityUser.builder()
            .userId(userId)
            .email(email)
            .authorities(
                List.of(new SimpleGrantedAuthority(role))
            )
            .build();

        return new UsernamePasswordAuthenticationToken(
            securityUser,
            null,
            securityUser.getAuthorities()
        );
    }
}
