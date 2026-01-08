package com.hafizbahtiar.spring.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security-related operations.
 */
@Slf4j
public class SecurityUtils {

    /**
     * Get the current authenticated user's principal.
     *
     * @return UserPrincipal of the current user
     * @throws IllegalStateException if user is not authenticated
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }

        throw new IllegalStateException("Authentication principal is not UserPrincipal");
    }

    /**
     * Get the current authenticated user's ID.
     *
     * @return User ID of the current user
     * @throws IllegalStateException if user is not authenticated
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if the current user is an admin.
     *
     * @return true if current user is admin, false otherwise
     */
    public static boolean isAdmin() {
        try {
            return getCurrentUser().isAdmin();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Check if the current user owns the resource (by user ID).
     *
     * @param resourceUserId The user ID of the resource owner
     * @return true if current user owns the resource or is admin, false otherwise
     */
    public static boolean ownsResource(Long resourceUserId) {
        try {
            UserPrincipal currentUser = getCurrentUser();
            return currentUser.isAdmin() || currentUser.ownsResource(resourceUserId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Check if the current user is authenticated.
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
