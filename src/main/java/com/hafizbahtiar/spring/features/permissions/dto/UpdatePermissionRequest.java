package com.hafizbahtiar.spring.features.permissions.dto;

import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a permission.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionRequest {

    /**
     * Type of permission (MODULE, PAGE, or COMPONENT)
     */
    private PermissionType permissionType;

    /**
     * Resource type
     */
    @Size(max = 50, message = "Resource type must not exceed 50 characters")
    private String resourceType;

    /**
     * Resource identifier
     */
    @Size(max = 200, message = "Resource identifier must not exceed 200 characters")
    private String resourceIdentifier;

    /**
     * Action allowed (READ, WRITE, DELETE, EXECUTE)
     */
    private PermissionAction action;

    /**
     * Whether this permission is granted (true) or denied (false)
     */
    private Boolean granted;
}
