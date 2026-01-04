package com.example.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Standard API success response")
public record ApiSuccessResponse (
    @JsonProperty("message")
    @Schema(description = "Success message", example = "Operation completed successfully")
    String message,

    @JsonProperty("success")
    @Schema(description = "Indicates if the operation was successful", example = "true")
    boolean success,

    @JsonProperty("httpStatusCode")
    @Schema(description = "HTTP status code", example = "200")
    Integer httpStatusCode,

    @JsonProperty("data")
    @Schema(description = "Response payload data")
    Object data
) {}
