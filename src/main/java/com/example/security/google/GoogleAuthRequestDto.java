package com.example.security.google;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequestDto(
    @NotBlank String idToken
) {}
