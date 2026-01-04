package com.example.security.magiclink;

import com.example.common.dto.ApiSuccessResponse;
import com.example.common.dto.ApiSuccessResponseCreator;
import com.example.security.dto.JwtResponse;
import com.example.security.dto.MagicLinkSendRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/magic-link")
@RequiredArgsConstructor
@Tag(name = "Magic Link Authentication", description = "Magic link authentication endpoints")
public class MagicLinkAuthController {

    private final MagicLinkAuthService magicLinkAuthService;
    private final ApiSuccessResponseCreator responseCreator;

    @PostMapping("/send")
    public ResponseEntity<ApiSuccessResponse> sendMagicLink(@Valid @RequestBody MagicLinkSendRequest request) {
        magicLinkAuthService.sendMagicLink(request.email());

        ApiSuccessResponse response = responseCreator.buildResponse(
            "Magic link sent successfully. Please check your email.",
            true,
            HttpStatus.OK
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiSuccessResponse> verifyMagicLink(
        @Parameter(description = "Magic link token", required = true)
        @RequestParam @NotBlank String token
    ) {
        JwtResponse jwtResponse = magicLinkAuthService.verifyMagicLink(token);

        ApiSuccessResponse response = responseCreator.buildResponse(
            "Login successful",
            true,
            HttpStatus.OK,
            jwtResponse
        );

        return ResponseEntity.ok(response);
    }
}
