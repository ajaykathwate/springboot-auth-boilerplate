package com.example.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Standard API error response")
public record ApiErrorResponse(

    @JsonProperty("message")
    @Schema(description = "Error message", example = "Validation failed")
    String message,

    @JsonProperty("httpStatusCode")
    @Schema(description = "HTTP status code", example = "400")
    Integer httpStatusCode,

    @JsonProperty("success")
    @Schema(description = "Always false for error responses", example = "false")
    boolean success,

    @JsonProperty("errors")
    @Schema(description = "Detailed error information")
    Object errors
) {}
