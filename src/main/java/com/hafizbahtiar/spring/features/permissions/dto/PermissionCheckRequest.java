package com.hafizbahtiar.spring.features.permissions.dto;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for checking if a user has a specific permission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckRequest {

    /**
     * Type of permission to check (MODULE, PAGE, or COMPONENT)
     */
    @NotNull(message = "Permission type is required")
    private PermissionType permissionType;

    /**
     * Resource type (e.g., "support", "finance", "portfolio", "admin")
     */
    @NotBlank(message = "Resource type is required")
    @Size(max = 50, message = "Resource type must not exceed 50 characters")
    private String resourceType;

    /**
     * Resource identifier (e.g., "chat", "tickets", "edit_button")
     * For MODULE: module key
     * For PAGE: page key
     * For COMPONENT: component key
     */
    @NotBlank(message = "Resource identifier is required")
    @Size(max = 200, message = "Resource identifier must not exceed 200 characters")
    private String resourceIdentifier;

    /**
     * Action to check (READ, WRITE, DELETE, EXECUTE)
     */
    @NotNull(message = "Permission action is required")
    private PermissionAction action;
}

