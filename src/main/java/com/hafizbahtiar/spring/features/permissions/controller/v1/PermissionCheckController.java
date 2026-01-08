package com.hafizbahtiar.spring.features.permissions.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.permissions.dto.PermissionCheckRequest;
import com.hafizbahtiar.spring.features.permissions.dto.PermissionCheckResponse;
import com.hafizbahtiar.spring.features.permissions.dto.UserPermissionsResponse;
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

/**
 * REST controller for permission checking endpoints.
 * Handles permission evaluation and user permission retrieval.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionCheckController {

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
     * Check if current user has a specific permission
     * POST /api/v1/permissions/check
     * Requires: Authenticated user
     */
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PermissionCheckResponse>> checkPermission(
            @Valid @RequestBody PermissionCheckRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.debug("Permission check request for user ID: {}, type: {}, resource: {}:{}, action: {}",
                userId, request.getPermissionType(), request.getResourceType(),
                request.getResourceIdentifier(), request.getAction());

        long startTime = System.currentTimeMillis();
        boolean hasPermission = permissionService.hasPermission(
                userId,
                request.getPermissionType(),
                request.getResourceType(),
                request.getResourceIdentifier(),
                request.getAction());
        long responseTime = System.currentTimeMillis() - startTime;

        // Log permission check
        permissionLoggingService.logPermissionChecked(userId, request.getPermissionType().name(),
                request.getResourceType(), request.getResourceIdentifier(), request.getAction().name(),
                hasPermission, httpRequest, responseTime);

        PermissionCheckResponse response = PermissionCheckResponse.builder()
                .hasPermission(hasPermission)
                .permissionType(request.getPermissionType())
                .resourceType(request.getResourceType())
                .resourceIdentifier(request.getResourceIdentifier())
                .action(request.getAction())
                .userId(userId)
                .message(hasPermission ? "User has the requested permission"
                        : "User does not have the requested permission")
                .build();

        return ResponseUtils.ok(response);
    }

    /**
     * Get current user's effective permissions
     * GET /api/v1/permissions/me
     * Requires: Authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> getMyPermissions() {
        Long userId = getCurrentUserId();
        log.debug("Fetching permissions for current user ID: {}", userId);
        UserPermissionsResponse response = permissionService.getUserPermissions(userId);
        return ResponseUtils.ok(response);
    }
}
