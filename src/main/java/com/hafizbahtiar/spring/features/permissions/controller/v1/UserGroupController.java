package com.hafizbahtiar.spring.features.permissions.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.permissions.dto.AssignUserRequest;
import com.hafizbahtiar.spring.features.permissions.dto.GroupResponse;
import com.hafizbahtiar.spring.features.permissions.service.PermissionLoggingService;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user-group assignment endpoints.
 * Handles assigning and removing users from permission groups.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
public class UserGroupController {

    private final PermissionService permissionService;
    private final UserService userService;
    private final PermissionLoggingService permissionLoggingService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Assign a user to a permission group
     * POST /api/v1/permissions/groups/{groupId}/users
     * Requires: OWNER or ADMIN role
     */
    @PostMapping("/groups/{groupId}/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignUserToGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody AssignUserRequest request,
            HttpServletRequest httpRequest) {
        Long assignedBy = getCurrentUserId();
        log.info("User assignment request received: user ID {} to group ID {} by user ID {}",
                request.getUserId(), groupId, assignedBy);
        long startTime = System.currentTimeMillis();
        try {
            permissionService.assignUserToGroup(groupId, request.getUserId(), assignedBy);
            long responseTime = System.currentTimeMillis() - startTime;
            UserResponse targetUser = userService.getById(request.getUserId());
            permissionLoggingService.logUserAssigned(groupId, assignedBy, request.getUserId(),
                    targetUser.getEmail(), httpRequest, responseTime);
            return ResponseUtils.ok(null, "User assigned to group successfully");
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logUserAssignmentEvent("USER_ASSIGNED", groupId, assignedBy,
                    request.getUserId(), null, httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all members of a permission group
     * GET /api/v1/permissions/groups/{groupId}/users
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/groups/{groupId}/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getGroupMembers(
            @PathVariable Long groupId) {
        log.debug("Fetching members for group ID: {}", groupId);
        List<UserResponse> members = permissionService.getGroupMembers(groupId).stream()
                .map(user -> userService.getById(user.getId()))
                .collect(Collectors.toList());
        return ResponseUtils.ok(members);
    }

    /**
     * Remove a user from a permission group
     * DELETE /api/v1/permissions/groups/{groupId}/users/{userId}
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/groups/{groupId}/users/{userId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        Long removedBy = getCurrentUserId();
        log.info("User removal request received: user ID {} from group ID {}", userId, groupId);
        long startTime = System.currentTimeMillis();
        try {
            UserResponse targetUser = userService.getById(userId);
            permissionService.removeUserFromGroup(groupId, userId);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logUserRemoved(groupId, removedBy, userId,
                    targetUser.getEmail(), httpRequest, responseTime);
            return ResponseUtils.noContent();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logUserAssignmentEvent("USER_REMOVED", groupId, removedBy,
                    userId, null, httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all permission groups for a user
     * GET /api/v1/permissions/users/{userId}/groups
     * Requires: OWNER or ADMIN role, or user can view own groups
     */
    @GetMapping("/users/{userId}/groups")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.ownsResource(#userId)")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getUserGroups(
            @PathVariable Long userId) {
        log.debug("Fetching groups for user ID: {}", userId);
        List<GroupResponse> groups = permissionService.getUserGroups(userId);
        return ResponseUtils.ok(groups);
    }
}
