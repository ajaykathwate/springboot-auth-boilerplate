package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token response containing access and refresh tokens")
public record JwtResponse(
    @Schema(description = "JWT access token for API authorization", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken
) {}
