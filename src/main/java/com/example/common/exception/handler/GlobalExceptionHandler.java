package com.example.common.exception.handler;

import com.example.common.dto.ApiErrorResponse;
import com.example.common.dto.ApiErrorResponseCreator;
import com.example.common.exception.FieldAlreadyExistsException;
import com.example.common.exception.ResourceAlreadyExistsException;
import com.example.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiErrorResponseCreator apiErrorResponseCreator;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception){
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return apiErrorResponseCreator.buildResponse(
            "Validation Failed",
            false,
            HttpStatus.BAD_REQUEST,
            errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolationException(final ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        return apiErrorResponseCreator.buildResponse(
            "Validation Failed",
            false,
            HttpStatus.BAD_REQUEST,
            errors
        );
    }

    @ExceptionHandler({
        ResourceNotFoundException.class,
        NoSuchElementException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResourceNotFoundException(final ResourceNotFoundException exception) {
        String errorMessage = exception.getMessage() != null ?
            exception.getMessage() :
            "The requested resource was not found";

        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("resource", errorMessage);

        return apiErrorResponseCreator.buildResponse(
            "Resource Not Found",
            false,
            HttpStatus.NOT_FOUND,
            errors
        );
    }

    @ExceptionHandler(FieldAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleFieldAlreadyExistsException(final FieldAlreadyExistsException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(exception.getResourceName().toLowerCase(), exception.getMessage());

        return apiErrorResponseCreator.buildResponse(
            "Field already exists",
            false,
            HttpStatus.CONFLICT,
            errors
        );
    }

    // Handle Database Errors (500)
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleDataAccessException(final DataAccessException exception) {
        return apiErrorResponseCreator.buildResponse(
            "Database operation failed",
            false,
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // Handle security-related exceptions (403)
    @ExceptionHandler({
        AccessDeniedException.class,
        DisabledException.class,
        LockedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleSecurityException(final Exception exception) {
        String message = switch (exception.getClass().getSimpleName()) {
            case "DisabledException" -> "Your account is disabled. Please contact support";
            case "LockedException" -> "Account temporarily locked due to multiple failed attempts";
            default -> "Access to this resource is restricted";
        };

        return apiErrorResponseCreator.buildResponse(
            message,
            false,
            HttpStatus.FORBIDDEN
        );
    }

    // Handle authentication failures (401)
    @ExceptionHandler({
        BadCredentialsException.class,
        UsernameNotFoundException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleAuthenticationException(final Exception exception) {
        return  apiErrorResponseCreator.buildResponse(
            "Bad Credentials",
            false,
            HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception) {
        String errorMessage = exception.getMessage() != null
            ? exception.getMessage()
            : "The resource already exists";

        return apiErrorResponseCreator.buildResponse(
            errorMessage,
            false,
            HttpStatus.CONFLICT
        );
    }

}
