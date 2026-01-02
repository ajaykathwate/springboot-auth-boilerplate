package com.example.security.exception;

public class InvalidGoogleTokenException extends RuntimeException {
    public InvalidGoogleTokenException(String message) {
        super(message);
    }
}
