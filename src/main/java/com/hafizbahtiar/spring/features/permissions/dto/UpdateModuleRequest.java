package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a permission module.
 * All fields are optional for partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModuleRequest {

    /**
     * Human-readable module name
     */
    @Size(max = 100, message = "Module name must not exceed 100 characters")
    private String moduleName;

    /**
     * Module description
     */
    private String description;

    /**
     * Roles that can assign this module to groups (comma-separated)
     * Examples: "OWNER", "ADMIN", "OWNER,ADMIN"
     * Must be valid roles: OWNER, ADMIN, USER
     */
    @Size(max = 50, message = "Available to roles must not exceed 50 characters")
    @Pattern(regexp = "^(OWNER|ADMIN|USER)(,(OWNER|ADMIN|USER))*$", 
            message = "Available to roles must be comma-separated valid roles (OWNER, ADMIN, USER)")
    private String availableToRoles;
}

