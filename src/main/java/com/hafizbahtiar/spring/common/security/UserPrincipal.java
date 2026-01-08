package com.hafizbahtiar.spring.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * UserPrincipal implementation for Spring Security.
 * Stores user information extracted from JWT token.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public static UserPrincipal create(Long id, String username, String email, String role) {
        return new UserPrincipal(id, username, email, role);
    }

    public boolean isOwner() {
        return Role.OWNER.getValue().equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.getValue().equalsIgnoreCase(role);
    }

    public boolean isOwnerOrAdmin() {
        return isOwner() || isAdmin();
    }

    public boolean isUser() {
        return Role.USER.getValue().equalsIgnoreCase(role);
    }

    public boolean ownsResource(Long resourceUserId) {
        return this.id != null && this.id.equals(resourceUserId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Password not stored in principal
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
