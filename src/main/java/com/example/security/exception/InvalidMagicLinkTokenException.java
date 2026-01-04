package com.example.security.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidMagicLinkTokenException extends AuthenticationException {

    public InvalidMagicLinkTokenException(String msg) {
        super(msg);
    }
}
