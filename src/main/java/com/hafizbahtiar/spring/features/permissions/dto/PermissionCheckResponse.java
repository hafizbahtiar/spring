package com.hafizbahtiar.spring.features.permissions.dto;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for permission check response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckResponse {

    /**
     * Whether the user has the requested permission
     */
    private Boolean hasPermission;

    /**
     * Permission type that was checked
     */
    private PermissionType permissionType;

    /**
     * Resource type that was checked
     */
    private String resourceType;

    /**
     * Resource identifier that was checked
     */
    private String resourceIdentifier;

    /**
     * Action that was checked
     */
    private PermissionAction action;

    /**
     * User ID that was checked
     */
    private Long userId;

    /**
     * Optional message explaining the result
     */
    private String message;
}

