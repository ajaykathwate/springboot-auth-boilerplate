package com.example.security.emailotp;

import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.security.dto.EmailOtpSendRequest;
import com.example.security.dto.EmailOtpVerifyRequest;
import com.example.security.dto.JwtResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-otp")
@RequiredArgsConstructor
@Tag(name = "Email OTP Authentication", description = "Email OTP authentication endpoints")
public class EmailOtpAuthController {

    private final EmailOtpAuthService emailOtpAuthService;
    private final ApiSuccessResponseCreator responseCreator;

    @PostMapping("/send")
    public ResponseEntity<ApiSuccessResponse> sendOtp(@Valid @RequestBody EmailOtpSendRequest request) {
        emailOtpAuthService.sendOtp(request.email());

        ApiSuccessResponse response = responseCreator.buildResponse(
            "OTP sent successfully. Please check your email.",
            true,
            HttpStatus.OK
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiSuccessResponse> verifyOtp(@Valid @RequestBody EmailOtpVerifyRequest request) {
        JwtResponse jwtResponse = emailOtpAuthService.verifyOtp(request.email(), request.otp());

        ApiSuccessResponse response = responseCreator.buildResponse(
            "Login successful",
            true,
            HttpStatus.OK,
            jwtResponse
        );

        return ResponseEntity.ok(response);
    }
}
