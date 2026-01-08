package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a permission module.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModuleRequest {

    /**
     * Unique module key (e.g., "support", "finance", "portfolio", "admin")
     * Must be lowercase alphanumeric with underscores, max 50 characters
     */
    @NotBlank(message = "Module key is required")
    @Size(max = 50, message = "Module key must not exceed 50 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Module key must be lowercase alphanumeric with underscores only")
    private String moduleKey;

    /**
     * Human-readable module name
     */
    @NotBlank(message = "Module name is required")
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
    @NotBlank(message = "Available to roles is required")
    @Size(max = 50, message = "Available to roles must not exceed 50 characters")
    @Pattern(regexp = "^(OWNER|ADMIN|USER)(,(OWNER|ADMIN|USER))*$", message = "Available to roles must be comma-separated valid roles (OWNER, ADMIN, USER)")
    private String availableToRoles;
}
