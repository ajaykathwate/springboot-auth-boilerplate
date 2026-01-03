package com.example.user.service;

import com.example.security.role.Role;
import com.example.user.entity.AuthProvider;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreateGoogleUser(
        String email,
        String firstName,
        String lastName,
        String googleUserId
    ) {

        return userRepository.findByEmail(email)
            .map(existingUser -> {

                if (existingUser.getProviderUserId() == null) {
                    existingUser.setProviderUserId(googleUserId);
                }

                if (!googleUserId.equals(existingUser.getProviderUserId())) {
                    throw new SecurityException("Google account mismatch");
                }

                existingUser.setFirstName(firstName);
                existingUser.setLastName(lastName);
                existingUser.setAuthProvider(AuthProvider.GOOGLE);
                existingUser.setEmailVerified(true);

                return userRepository.save(existingUser);
            })
            .orElseGet(() -> createGoogleUser(email, firstName, lastName, googleUserId));
    }

    private User createGoogleUser(
        String email,
        String firstName,
        String lastName,
        String googleUserId
    ) {
        return userRepository.save(
            User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .authProvider(AuthProvider.GOOGLE)
                .providerUserId(googleUserId)
                .emailVerified(true)
                .role(Role.ROLE_USER)
                .build()
        );
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() ->
                new IllegalStateException(
                    "User not found for email: " + email
                )
            );
    }
}
