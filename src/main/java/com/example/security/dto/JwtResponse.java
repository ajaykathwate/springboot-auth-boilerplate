package com.example.security.dto;

public record JwtResponse(
    String accessToken,
    String refreshToken,
    long expiresInSeconds
) {}
