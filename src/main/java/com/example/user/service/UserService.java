package com.example.user.service;

import com.example.common.exception.ResourceNotFoundException;
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

    public User findOrCreateGoogleUser(String email, String firstName, String lastName, String googleUserId) {
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

    private User createGoogleUser(String email, String firstName, String lastName, String googleUserId) {
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

    public User findOrCreateEmailOtpUser(String email) {
        return userRepository.findByEmail(email)
            .map(existingUser -> {
                // If already verified → update provider info only
                if (existingUser.isEmailVerified()) {
                    existingUser.setAuthProvider(AuthProvider.EMAIL_OTP);
                    existingUser.setProviderUserId(null);
                    return userRepository.save(existingUser);
                }

                // If NOT verified → update Provider and do NOT verify here
                existingUser.setAuthProvider(AuthProvider.EMAIL_OTP);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> createEmailOtpUser(email));
    }

    private User createEmailOtpUser(String email) {
        String localPart = email.substring(0, email.indexOf('@'));
        return userRepository.save(
            User.builder()
                .email(email)
                .firstName(localPart)
                .lastName("")
                .authProvider(AuthProvider.EMAIL_OTP)
                .providerUserId(null)
                .emailVerified(false)
                .role(Role.ROLE_USER)
                .build()
        );
    }

    public User findOrCreateMagicLinkUser(String email) {
        return userRepository.findByEmail(email)
            .map(existingUser -> {
                // If already verified → update provider info only
                if (existingUser.isEmailVerified()) {
                    existingUser.setAuthProvider(AuthProvider.MAGIC_LINK);
                    existingUser.setProviderUserId(null);
                    return userRepository.save(existingUser);
                }

                // If NOT verified → update Provider and do NOT verify here
                existingUser.setAuthProvider(AuthProvider.MAGIC_LINK);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> createMagicLinkUser(email));
    }

    private User createMagicLinkUser(String email) {
        String localPart = email.substring(0, email.indexOf('@'));
        return userRepository.save(
            User.builder()
                .email(email)
                .firstName(localPart)
                .lastName("")
                .authProvider(AuthProvider.MAGIC_LINK)
                .providerUserId(null)
                .emailVerified(true)
                .role(Role.ROLE_USER)
                .build()
        );
    }

    public void markUserAsVerified(String email){
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (user.isEmailVerified()) {
            return;
        }

        user.setEmailVerified(true);
    }
}
