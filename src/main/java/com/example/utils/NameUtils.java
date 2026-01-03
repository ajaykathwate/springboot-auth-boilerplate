package com.example.utils;

import java.util.Arrays;

public final class NameUtils {

    private NameUtils() {}

    public static NameParts extractNameParts(String fullName, String email) {

        if (fullName == null || fullName.isBlank()) {
            String fallback = email.substring(0, email.indexOf('@'));
            return new NameParts(fallback, "");
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return new NameParts(parts[0], "");
        }

        String firstName = parts[0];
        String lastName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        return new NameParts(firstName, lastName);
    }
}
