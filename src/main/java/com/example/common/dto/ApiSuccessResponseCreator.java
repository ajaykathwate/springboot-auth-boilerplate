package com.example.common.dto;

import jakarta.annotation.Nullable;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ApiSuccessResponseCreator {
    public ApiSuccessResponse buildResponse(String successMessage, boolean success, HttpStatus httpStatus, @Nullable Object data) {
        return ApiSuccessResponse.builder()
            .message(successMessage)
            .success(success)
            .httpStatusCode(httpStatus.value())
            .data(data)
            .build();
    }

    // Overload without errors
    public ApiSuccessResponse buildResponse(String successMessage, boolean success, HttpStatus httpStatus) {
        return buildResponse(successMessage, success, httpStatus, Collections.emptyMap());
    }
}
