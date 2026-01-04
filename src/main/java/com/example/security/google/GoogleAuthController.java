package com.example.security.google;

import com.example.common.dto.ApiErrorResponse;
import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.security.dto.GoogleAuthRequestDto;
import com.example.security.dto.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@Tag(name = "Google Authentication", description = "Google OAuth2 authentication endpoints")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final ApiSuccessResponseCreator responseCreator;

    @Operation(summary = "Login with Google", description = "Authenticate user using Google OAuth2 ID token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid Google token",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
