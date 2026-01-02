package com.example.security.role;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public String authority() {
        return this.name();
    }
}
