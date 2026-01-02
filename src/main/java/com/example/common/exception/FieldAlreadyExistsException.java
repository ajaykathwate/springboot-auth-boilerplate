package com.example.common.exception;


import lombok.Data;

@Data
public class FieldAlreadyExistsException extends RuntimeException{
    private final String resourceName;
    private final String fieldName;
    private final String fieldValue;

    public FieldAlreadyExistsException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s with '%s' %s already exists",resourceName, fieldValue, fieldName));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
