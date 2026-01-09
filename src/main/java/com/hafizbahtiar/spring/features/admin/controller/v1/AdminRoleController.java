package com.hafizbahtiar.spring.features.admin.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import com.hafizbahtiar.spring.features.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for admin role management endpoints.
 * Handles viewing role statistics and users by role.
 */
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Slf4j
public class AdminRoleController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Get all roles with statistics
     * GET /api/v1/admin/roles
     * Requires: users.roles page READ permission OR OWNER/ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'users', 'users.roles', 'READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoles() {
        log.debug("GET /api/v1/admin/roles - Getting role statistics");

        Map<String, Object> roleStats = new HashMap<>();

        // Get user counts for each role
        Long ownerCount = userRepository.countByRole("OWNER");
        Long adminCount = userRepository.countByRole("ADMIN");
        Long userCount = userRepository.countByRole("USER");

        // Build role statistics
        roleStats.put("OWNER", Map.of(
                "name", "OWNER",
                "description", "System owner with full access (unique role)",
                "userCount", ownerCount != null ? ownerCount : 0L,
                "unique", true));

        roleStats.put("ADMIN", Map.of(
                "name", "ADMIN",
                "description", "Administrator with admin features access",
                "userCount", adminCount != null ? adminCount : 0L,
                "unique", false));

        roleStats.put("USER", Map.of(
                "name", "USER",
                "description", "Standard user with basic permissions",
                "userCount", userCount != null ? userCount : 0L,
                "unique", false));

        return ResponseUtils.ok(roleStats);
    }

    /**
     * Get users by role
     * GET /api/v1/admin/roles/{role}/users
     * Requires: users.roles page READ permission OR OWNER/ADMIN role
     */
    @GetMapping("/{role}/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'users', 'users.roles', 'READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.debug("GET /api/v1/admin/roles/{}/users - Getting users by role", role);

        // Validate role
        if (!role.equalsIgnoreCase("OWNER") && !role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("USER")) {
            throw new IllegalArgumentException("Invalid role. Must be OWNER, ADMIN, or USER");
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get users with role filter
        Page<UserResponse> users = userService.getUsersWithFilters(null, role.toUpperCase(), null, pageable);

        return ResponseUtils.ok(users);
    }

    /**
     * Get role permissions overview (if applicable)
     * GET /api/v1/admin/roles/{role}/permissions
     * Requires: users.roles page READ permission OR OWNER/ADMIN role
     * 
     * Note: In the current system, roles are static and permissions are managed
     * through
     * permission groups (Layer 2). This endpoint returns basic role information.
     */
    @GetMapping("/{role}/permissions")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'users', 'users.roles', 'READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRolePermissions(@PathVariable String role) {
        log.debug("GET /api/v1/admin/roles/{}/permissions - Getting role permissions", role);

        // Validate role
        if (!role.equalsIgnoreCase("OWNER") && !role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("USER")) {
            throw new IllegalArgumentException("Invalid role. Must be OWNER, ADMIN, or USER");
        }

        Map<String, Object> permissions = new HashMap<>();
        permissions.put("role", role.toUpperCase());
        permissions.put("description", getRoleDescription(role.toUpperCase()));
        permissions.put("note", "Roles are static. Permissions are managed through permission groups (Layer 2).");
        permissions.put("userCount", userRepository.countByRole(role.toUpperCase()));

        return ResponseUtils.ok(permissions);
    }

    private String getRoleDescription(String role) {
        return switch (role) {
            case "OWNER" -> "System owner with full access (unique role)";
            case "ADMIN" -> "Administrator with admin features access";
            case "USER" -> "Standard user with basic permissions";
            default -> "Unknown role";
        };
    }
}
