package com.hafizbahtiar.spring.features.permissions.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.permissions.dto.CreateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.dto.GroupResponse;
import com.hafizbahtiar.spring.features.permissions.dto.UpdateGroupRequest;
import com.hafizbahtiar.spring.features.permissions.service.PermissionLoggingService;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
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

/**
 * REST controller for permission group management endpoints.
 * Handles group CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/permissions/groups")
@RequiredArgsConstructor
@Slf4j
public class PermissionGroupController {

    private final PermissionService permissionService;
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
     * Create a new permission group
     * POST /api/v1/permissions/groups
     * Requires: OWNER or ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            HttpServletRequest httpRequest) {
        Long createdBy = getCurrentUserId();
        log.info("Permission group creation request received: {} by user ID: {}", request.getName(), createdBy);
        long startTime = System.currentTimeMillis();
        try {
            GroupResponse response = permissionService.createGroup(request, createdBy);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logGroupCreated(response.getId(), createdBy, response.getName(),
                    httpRequest, responseTime);
            return ResponseUtils.created(response, "Permission group created successfully");
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logGroupEvent("GROUP_CREATED", null, createdBy, null, null,
                    request.getName(), null, null, null, null, httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all permission groups
     * GET /api/v1/permissions/groups
     * Requires: OWNER or ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getAllGroups() {
        log.debug("Fetching all permission groups");
        List<GroupResponse> groups = permissionService.getAllGroups();
        return ResponseUtils.ok(groups);
    }

    /**
     * Get permission group by ID
     * GET /api/v1/permissions/groups/{id}
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(@PathVariable Long id) {
        log.debug("Fetching permission group ID: {}", id);
        GroupResponse response = permissionService.getGroupById(id);
        return ResponseUtils.ok(response);
    }

    /**
     * Update an existing permission group
     * PUT /api/v1/permissions/groups/{id}
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Permission group update request received for ID: {}", id);
        long startTime = System.currentTimeMillis();
        try {
            GroupResponse response = permissionService.updateGroup(id, request);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logGroupUpdated(id, userId, response.getName(), httpRequest, responseTime);
            return ResponseUtils.ok(response, "Permission group updated successfully");
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logGroupEvent("GROUP_UPDATED", id, userId, null, null,
                    request.getName(), null, null, null, null, httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a permission group
     * DELETE /api/v1/permissions/groups/{id}
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Permission group deletion request received for ID: {}", id);
        long startTime = System.currentTimeMillis();
        try {
            // Get group name before deletion for logging
            GroupResponse group = permissionService.getGroupById(id);
            permissionService.deleteGroup(id);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logGroupDeleted(id, userId, group.getName(), httpRequest, responseTime);
            return ResponseUtils.noContent();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logGroupEvent("GROUP_DELETED", id, userId, null, null,
                    null, null, null, null, null, httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }
}
