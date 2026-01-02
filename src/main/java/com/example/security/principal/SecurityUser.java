package com.example.security.principal;

import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null; // OTP / OAuth2 → no password
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
