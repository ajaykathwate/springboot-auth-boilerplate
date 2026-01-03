package com.example.security.auth;

import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.security.dto.JwtResponse;
import com.example.security.dto.RefreshTokenRequest;
import com.example.security.jwt.JwtTokenProvider;
import com.example.security.jwt.RefreshTokenService;
import com.example.security.jwt.TokenService;
import com.example.user.User;
import com.example.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ApiSuccessResponseCreator responseCreator;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final TokenService tokenService;

    private final HttpServletRequest httpRequest;

    @PostMapping("/refresh")
    public ResponseEntity<ApiSuccessResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Logged into /refresh method....");
        String refreshToken = request.refreshToken();

        String email = refreshTokenService.validateAndGetEmail(refreshToken);

        User user = userService.getByEmail(email);

        JwtResponse jwtResponse =
            tokenService.rotateTokens(refreshToken, user);

        return ResponseEntity.ok(
            responseCreator.buildResponse(
                "Token refreshed successfully",
                true,
                HttpStatus.OK,
                jwtResponse
            )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponse> logout(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest httpRequest
    ) {
        // 1️⃣ Invalidate refresh token if present
        if (request != null && request.refreshToken() != null) {
            refreshTokenService.invalidate(request.refreshToken());
        }

        return ResponseEntity.ok(
            responseCreator.buildResponse(
                "Logged out successfully",
                true,
                HttpStatus.OK,
                null
            )
        );
    }
}
