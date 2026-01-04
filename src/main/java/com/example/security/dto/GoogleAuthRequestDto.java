package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for Google OAuth2 authentication")
public record GoogleAuthRequestDto(
    @Schema(description = "Google OAuth2 ID token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String idToken
) {}
