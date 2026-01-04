package com.example.security.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidOtpException extends AuthenticationException {

    public InvalidOtpException(String msg) {
        super(msg);
    }
}
