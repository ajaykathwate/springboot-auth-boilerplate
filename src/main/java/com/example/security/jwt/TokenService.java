package com.example.security.jwt;

import com.example.security.dto.JwtResponse;
import com.example.user.entity.User;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public JwtResponse issueTokens(User user) {

        Map<String, Object> claims = Map.of(
            "userId", user.getId(),
            "role", user.getRole().authority()
        );

        String accessToken =
            jwtTokenProvider.generateToken(claims, user.getEmail());

        String refreshToken =
            jwtTokenProvider.generateRefreshToken(user.getEmail());

        refreshTokenService.store(refreshToken, user.getEmail());

        return new JwtResponse(
            accessToken,
            refreshToken
        );
    }

    public JwtResponse rotateTokens(String oldRefreshToken, User user) {

        refreshTokenService.invalidate(oldRefreshToken);

        return issueTokens(user);
    }
}
