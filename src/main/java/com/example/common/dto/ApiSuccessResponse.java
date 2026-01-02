package com.example.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ApiSuccessResponse (
    @JsonProperty("message")
    String message,

    @JsonProperty("success")
    boolean success,

    @JsonProperty("httpStatusCode")
    Integer httpStatusCode,

    @JsonProperty("data")
    Object data
) {}
