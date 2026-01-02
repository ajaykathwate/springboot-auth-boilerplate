package com.example.security.google;

import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.security.dto.JwtResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final ApiSuccessResponseCreator responseCreator;

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse> login(@Valid @RequestBody GoogleAuthRequestDto request) {
        JwtResponse jwtResponse = googleAuthService.loginWithGoogle(request.idToken());

        ApiSuccessResponse response =
            responseCreator.buildResponse(
                "Login successful",
                true,
                HttpStatus.OK,
                jwtResponse
            );

        return ResponseEntity.ok(response);
    }
}
