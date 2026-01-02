package com.example.common.dto;

import jakarta.annotation.Nullable;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ApiErrorResponseCreator {

    public ApiErrorResponse buildResponse(String errorMessage, boolean success, HttpStatus httpStatus, @Nullable Object errors) {
        return ApiErrorResponse.builder()
            .message(errorMessage)
            .success(success)
            .httpStatusCode(httpStatus.value())
            .errors(errors)
            .build();
    }

    // Overload without errors
    public ApiErrorResponse buildResponse(String msg, boolean success, HttpStatus httpStatus) {
        return buildResponse(msg, success, httpStatus, Collections.emptyMap());
    }
}
