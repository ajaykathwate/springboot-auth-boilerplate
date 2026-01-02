package com.example.security.exception.handler;

import com.example.common.dto.ApiErrorResponse;
import com.example.common.dto.ApiErrorResponseCreator;
import com.example.security.exception.InvalidGoogleTokenException;
import com.example.security.exception.JwtTokenException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class JwtTokenExceptionsHandler {

    private final ApiErrorResponseCreator apiErrorResponseCreator;

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<Map<String, String>> handleJwtTokenException(final JwtTokenException exception) {
        Map<String, String> errors = new HashMap<>();
        errors.put("JwtToken Error message", exception.getMessage());
        errors.put("JwtToken Cause Error message", exception.getCause() != null ? exception.getCause().getMessage() : "n/a");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ApiErrorResponse handleInvalidGoogleToken(
        InvalidGoogleTokenException ex
    ) {
        return apiErrorResponseCreator.buildResponse(
            ex.getMessage(),
            false,
            HttpStatus.UNAUTHORIZED
        );
    }
}
