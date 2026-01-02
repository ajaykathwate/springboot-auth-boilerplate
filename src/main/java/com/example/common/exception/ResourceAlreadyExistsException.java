package com.example.common.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus(HttpStatus.OK)
public class ResourceAlreadyExistsException extends RuntimeException{

    private final String resourceName;
    private final String fieldName;

    public ResourceAlreadyExistsException(String resourceName, String fieldName) {
        super(String.format("%s with this %s already exists", resourceName, fieldName));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
    }
}

