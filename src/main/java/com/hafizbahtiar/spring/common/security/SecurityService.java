package com.hafizbahtiar.spring.common.security;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service bean for security operations that can be used in SpEL expressions.
 * This is needed because @PreAuthorize annotations can reference Spring beans.
 * 
 * This service integrates both Layer 1 (static roles) and Layer 2 (group permissions)
 * for comprehensive authorization checks.
 */
@Service("securityUtils")
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final PermissionService permissionService;

    /**
     * Check if the current user owns the resource (by user ID).
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @param resourceUserId The user ID of the resource owner
     * @return true if current user owns the resource or is admin, false otherwise
     */
    public boolean ownsResource(Long resourceUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                // Owner and Admin can access any resource, regular users can only access their own
                return userPrincipal.isOwnerOrAdmin() || userPrincipal.ownsResource(resourceUserId);
            }

            return false;
        } catch (Exception e) {
            log.debug("Error checking resource ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user is an owner.
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @return true if current user is owner, false otherwise
     */
    public boolean isOwner() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.isOwner();
            }

            return false;
        } catch (Exception e) {
            log.debug("Error checking owner status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user is an admin.
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @return true if current user is admin, false otherwise
     */
    public boolean isAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.isAdmin();
            }

            return false;
        } catch (Exception e) {
            log.debug("Error checking admin status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user is an owner or admin.
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @return true if current user is owner or admin, false otherwise
     */
    public boolean isOwnerOrAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof UserPrincipal userPrincipal) {
                return userPrincipal.isOwnerOrAdmin();
            }

            return false;
        } catch (Exception e) {
            log.debug("Error checking owner/admin status: {}", e.getMessage());
            return false;
        }
    }

    // ==========================================
    // Layer 2: Group Permission Checks
    // ==========================================

    /**
     * Check if the current user has a specific permission.
     * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @param permissionType     Permission type (MODULE, PAGE, COMPONENT)
     * @param resourceType       Resource type (e.g., "support", "finance")
     * @param resourceIdentifier Resource identifier (e.g., "chat", "tickets", "edit_button")
     * @param action             Permission action (READ, WRITE, DELETE, EXECUTE)
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(String permissionType, String resourceType, String resourceIdentifier,
            String action) {
        try {
            Long userId = getCurrentUserId();
            PermissionType type = PermissionType.valueOf(permissionType.toUpperCase());
            PermissionAction actionEnum = PermissionAction.valueOf(action.toUpperCase());
            return permissionService.hasPermission(userId, type, resourceType, resourceIdentifier, actionEnum);
        } catch (Exception e) {
            log.debug("Error checking permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user has access to a module.
     * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @param moduleKey Module key (e.g., "support", "finance")
     * @return true if user has module access, false otherwise
     */
    public boolean hasModuleAccess(String moduleKey) {
        try {
            Long userId = getCurrentUserId();
            return permissionService.hasModuleAccess(userId, moduleKey);
        } catch (Exception e) {
            log.debug("Error checking module access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user has access to a page.
     * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @param moduleKey Module key (e.g., "support", "finance")
     * @param pageKey   Page key (e.g., "support.chat", "finance.dashboard")
     * @return true if user has page access, false otherwise
     */
    public boolean hasPageAccess(String moduleKey, String pageKey) {
        try {
            Long userId = getCurrentUserId();
            return permissionService.hasPageAccess(userId, moduleKey, pageKey);
        } catch (Exception e) {
            log.debug("Error checking page access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user has access to a component.
     * Checks Layer 1 (static role) first, then Layer 2 (group permissions).
     * This method can be used in @PreAuthorize SpEL expressions.
     *
     * @param pageKey      Page key (e.g., "support.chat", "finance.dashboard")
     * @param componentKey Component key (e.g., "edit_button", "delete_button")
     * @return true if user has component access, false otherwise
     */
    public boolean hasComponentAccess(String pageKey, String componentKey) {
        try {
            Long userId = getCurrentUserId();
            return permissionService.hasComponentAccess(userId, pageKey, componentKey);
        } catch (Exception e) {
            log.debug("Error checking component access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current authenticated user ID.
     * Helper method for permission checks.
     *
     * @return Current user ID
     * @throws IllegalStateException if user is not authenticated
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        throw new IllegalStateException("Authentication principal is not UserPrincipal");
    }
}

