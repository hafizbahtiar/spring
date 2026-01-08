package com.hafizbahtiar.spring.features.permissions.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.permissions.dto.AddPermissionRequest;
import com.hafizbahtiar.spring.features.permissions.dto.PermissionResponse;
import com.hafizbahtiar.spring.features.permissions.dto.UpdatePermissionRequest;
import com.hafizbahtiar.spring.features.permissions.service.PermissionLoggingService;
import com.hafizbahtiar.spring.features.permissions.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for permission management endpoints.
 * Handles permission CRUD operations within groups.
 */
@RestController
@RequestMapping("/api/v1/permissions/groups/{groupId}/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionLoggingService permissionLoggingService;

    /**
     * Add a permission to a permission group
     * POST /api/v1/permissions/groups/{groupId}/permissions
     * Requires: OWNER or ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> addPermission(
            @PathVariable Long groupId,
            @Valid @RequestBody AddPermissionRequest request,
            HttpServletRequest httpRequest) {
        log.info("Permission addition request received for group ID: {}, type: {}, resource: {}:{}",
                groupId, request.getPermissionType(), request.getResourceType(), request.getResourceIdentifier());
        long startTime = System.currentTimeMillis();
        try {
            PermissionResponse response = permissionService.addPermission(groupId, request);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logPermissionAdded(groupId, response.getId(), null,
                    request.getPermissionType().name(), request.getResourceType(),
                    request.getResourceIdentifier(), request.getAction().name(), httpRequest, responseTime);
            return ResponseUtils.created(response, "Permission added successfully");
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logPermissionEvent("PERMISSION_ADDED", groupId, null, null, null,
                    null, request.getPermissionType().name(), request.getResourceType(),
                    request.getResourceIdentifier(), request.getAction().name(), httpRequest, responseTime,
                    false, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all permissions for a permission group
     * GET /api/v1/permissions/groups/{groupId}/permissions
     * Requires: OWNER or ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getGroupPermissions(
            @PathVariable Long groupId) {
        log.debug("Fetching permissions for group ID: {}", groupId);
        List<PermissionResponse> permissions = permissionService.getGroupPermissions(groupId);
        return ResponseUtils.ok(permissions);
    }

    /**
     * Update an existing permission
     * PUT /api/v1/permissions/groups/{groupId}/permissions/{id}
     * Requires: OWNER or ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long groupId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request,
            HttpServletRequest httpRequest) {
        log.info("Permission update request received for ID: {} in group ID: {}", id, groupId);
        long startTime = System.currentTimeMillis();
        try {
            PermissionResponse response = permissionService.updatePermission(id, request);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logPermissionUpdated(groupId, id, null,
                    request.getPermissionType() != null ? request.getPermissionType().name() : null,
                    request.getResourceType(), request.getResourceIdentifier(),
                    request.getAction() != null ? request.getAction().name() : null, httpRequest, responseTime);
            return ResponseUtils.ok(response, "Permission updated successfully");
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logPermissionEvent("PERMISSION_UPDATED", groupId, id, null, null,
                    null, request.getPermissionType() != null ? request.getPermissionType().name() : null,
                    request.getResourceType(), request.getResourceIdentifier(),
                    request.getAction() != null ? request.getAction().name() : null, httpRequest, responseTime,
                    false, e.getMessage());
            throw e;
        }
    }

    /**
     * Remove a permission from a group
     * DELETE /api/v1/permissions/groups/{groupId}/permissions/{id}
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removePermission(
            @PathVariable Long groupId,
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        log.info("Permission removal request received for ID: {} in group ID: {}", id, groupId);
        long startTime = System.currentTimeMillis();
        try {
            permissionService.removePermission(id);
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logPermissionRemoved(groupId, id, null, httpRequest, responseTime);
            return ResponseUtils.noContent();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            permissionLoggingService.logPermissionEvent("PERMISSION_REMOVED", groupId, id, null, null,
                    null, null, null, null, null, httpRequest, responseTime, false, e.getMessage());
            throw e;
        }
    }
}
