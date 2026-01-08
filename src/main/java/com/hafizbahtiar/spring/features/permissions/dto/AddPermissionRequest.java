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
 * DTO for adding a permission to a group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPermissionRequest {

    /**
     * Type of permission (MODULE, PAGE, or COMPONENT)
     */
    @NotNull(message = "Permission type is required")
    private PermissionType permissionType;

    /**
     * Resource type (e.g., "support", "finance", "portfolio", "admin")
     * For MODULE: the module key
     * For PAGE: the module key
     * For COMPONENT: the module key
     */
    @NotBlank(message = "Resource type is required")
    @Size(max = 50, message = "Resource type must not exceed 50 characters")
    private String resourceType;

    /**
     * Resource identifier (e.g., "chat", "tickets", "edit_button")
     * For MODULE: empty or module key
     * For PAGE: the page key (e.g., "chat", "tickets")
     * For COMPONENT: the component key (e.g., "edit_button", "delete_button")
     */
    @NotBlank(message = "Resource identifier is required")
    @Size(max = 200, message = "Resource identifier must not exceed 200 characters")
    private String resourceIdentifier;

    /**
     * Action allowed (READ, WRITE, DELETE, EXECUTE)
     */
    @NotNull(message = "Permission action is required")
    private PermissionAction action;

    /**
     * Whether this permission is granted (true) or denied (false)
     * true = allow access
     * false = explicitly deny access (overrides allow permissions)
     */
    @Builder.Default
    private Boolean granted = true;
}
