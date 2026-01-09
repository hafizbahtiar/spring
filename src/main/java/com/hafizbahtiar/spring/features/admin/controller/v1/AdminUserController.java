package com.hafizbahtiar.spring.features.admin.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.admin.dto.AdminUserCreateRequest;
import com.hafizbahtiar.spring.features.admin.dto.AdminUserUpdateRequest;
import com.hafizbahtiar.spring.features.admin.dto.ChangeRoleRequest;
import com.hafizbahtiar.spring.features.permissions.dto.GroupResponse;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.dto.UserUpdateRequest;
import com.hafizbahtiar.spring.features.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin user management endpoints.
 * Handles CRUD operations for users with proper permission checks.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;
    private final PermissionService permissionService;

    /**
     * List users with filters/pagination/search
     * GET /api/v1/admin/users
     * Requires: users.list page READ permission OR OWNER/ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'users', 'users.list', 'READ')")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.debug("GET /api/v1/admin/users - search={}, role={}, active={}, page={}, size={}",
                search, role, active, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.getUsersWithFilters(search, role, active, pageable);
        PaginatedResponse<UserResponse> paginatedResponse = PaginatedResponse.fromPage(users);

        return ResponseUtils.ok(paginatedResponse);
    }

    /**
     * Get user details by ID
     * GET /api/v1/admin/users/{id}
     * Requires: users.list page READ permission OR OWNER/ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'users', 'users.list', 'READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.debug("GET /api/v1/admin/users/{} - Getting user details", id);
        UserResponse user = userService.getById(id);
        return ResponseUtils.ok(user);
    }

    /**
     * Create new user
     * POST /api/v1/admin/users
     * Requires: create_user component permission OR OWNER/ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('COMPONENT', 'users', 'users.list', 'create_user')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        log.info("POST /api/v1/admin/users - Creating new user with email: {}", request.getEmail());

        UserResponse user = userService.createUser(
                request.getEmail(),
                request.getUsername(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getRole(),
                request.getActive() != null ? request.getActive() : true);

        return ResponseUtils.created(user, "User created successfully");
    }

    /**
     * Update user
     * PUT /api/v1/admin/users/{id}
     * Requires: edit_user component permission OR OWNER/ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('COMPONENT', 'users', 'users.list', 'edit_user')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        log.info("PUT /api/v1/admin/users/{} - Updating user", id);

        // Convert AdminUserUpdateRequest to UserUpdateRequest
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail(request.getEmail());
        updateRequest.setUsername(request.getUsername());
        updateRequest.setFirstName(request.getFirstName());
        updateRequest.setLastName(request.getLastName());
        updateRequest.setPhone(request.getPhone());

        UserResponse user = userService.updateUser(id, updateRequest);

        // Update active status if provided
        if (request.getActive() != null) {
            user = userService.updateUserActiveStatus(id, request.getActive());
        }

        return ResponseUtils.ok(user, "User updated successfully");
    }

    /**
     * Delete/deactivate user
     * DELETE /api/v1/admin/users/{id}
     * Requires: delete_user component permission OR OWNER/ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('COMPONENT', 'users', 'users.list', 'delete_user')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/v1/admin/users/{} - Deleting/deactivating user", id);

        // Prevent deleting OWNER user
        UserResponse user = userService.getById(id);
        if ("OWNER".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(
                    "Cannot delete user with OWNER role. OWNER role is unique and cannot be removed.");
        }

        userService.deleteUser(id);
        return ResponseUtils.noContent();
    }

    /**
     * Change user role
     * PATCH /api/v1/admin/users/{id}/role
     * Requires: change_role component permission OR OWNER/ADMIN role
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('COMPONENT', 'users', 'users.list', 'change_role')")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request) {
        log.info("PATCH /api/v1/admin/users/{}/role - Changing role to {}", id, request.getRole());

        UserResponse user = userService.updateUserRole(id, request.getRole());
        return ResponseUtils.ok(user, "User role updated successfully");
    }

    /**
     * Get user's permission groups
     * GET /api/v1/admin/users/{id}/groups
     * Requires: users.list page READ permission OR OWNER/ADMIN role
     */
    @GetMapping("/{id}/groups")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'users', 'users.list', 'READ')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getUserPermissionGroups(@PathVariable Long id) {
        log.debug("GET /api/v1/admin/users/{}/groups - Getting user's permission groups", id);
        List<GroupResponse> groups = permissionService.getUserGroups(id);
        return ResponseUtils.ok(groups);
    }

    /**
     * Assign permission groups to user
     * POST /api/v1/admin/users/{id}/groups
     * Requires: OWNER/ADMIN role (group assignment is handled by permission
     * service)
     */
    @PostMapping("/{id}/groups")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignPermissionGroups(
            @PathVariable Long id,
            @RequestBody List<Long> groupIds) {
        log.info("POST /api/v1/admin/users/{}/groups - Assigning {} groups to user", id, groupIds.size());

        for (Long groupId : groupIds) {
            permissionService.assignUserToGroup(groupId, id, getCurrentUserId());
        }

        return ResponseUtils.ok(null, "Permission groups assigned successfully");
    }

    /**
     * Remove permission group from user
     * DELETE /api/v1/admin/users/{id}/groups/{groupId}
     * Requires: OWNER/ADMIN role
     */
    @DeleteMapping("/{id}/groups/{groupId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removePermissionGroup(
            @PathVariable Long id,
            @PathVariable Long groupId) {
        log.info("DELETE /api/v1/admin/users/{}/groups/{} - Removing group from user", id, groupId);

        permissionService.removeUserFromGroup(groupId, id);
        return ResponseUtils.noContent();
    }

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication
                .getPrincipal() instanceof com.hafizbahtiar.spring.common.security.UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }
}
