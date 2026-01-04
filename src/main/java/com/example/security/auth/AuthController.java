package com.example.security.auth;

import com.example.common.dto.ApiErrorResponse;
import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.security.dto.JwtResponse;
import com.example.security.dto.RefreshTokenRequest;
import com.example.security.jwt.RefreshTokenService;
import com.example.security.jwt.TokenService;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {

    private final ApiSuccessResponseCreator responseCreator;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final TokenService tokenService;

    private final HttpServletRequest httpRequest;

    @Operation(summary = "Refresh access token", description = "Exchange a valid refresh token for a new access token and refresh token pair")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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

    @Operation(summary = "Logout user", description = "Invalidate the refresh token to logout the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logged out successfully",
            content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponse> logout(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest httpRequest
    ) {
        // Invalidate refresh token if present
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
