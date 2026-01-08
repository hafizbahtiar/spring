package com.hafizbahtiar.spring.features.permissions.dto;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for user's effective permissions response.
 * Contains all permissions a user has access to, aggregated from their groups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsResponse {

    private Long userId;
    private String userEmail;
    private String userRole;

    /**
     * List of permission groups the user belongs to
     */
    private List<GroupResponse> groups;

    /**
     * Effective permissions grouped by resource type and identifier.
     * Key format: "permissionType:resourceType:resourceIdentifier"
     * Value: Map of action -> granted (true/false)
     * Example: "MODULE:support:" -> {READ: true, WRITE: true}
     */
    private Map<String, Map<PermissionAction, Boolean>> effectivePermissions;

    /**
     * Summary of permissions by type
     */
    private PermissionSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionSummary {
        private Integer totalModulePermissions;
        private Integer totalPagePermissions;
        private Integer totalComponentPermissions;
        private Integer totalGranted;
        private Integer totalDenied;
    }
}
