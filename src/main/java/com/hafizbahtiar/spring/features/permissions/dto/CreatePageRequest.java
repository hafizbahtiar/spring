package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a permission page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageRequest {

    /**
     * Module key this page belongs to (e.g., "support", "finance", "portfolio")
     */
    @NotBlank(message = "Module key is required")
    @Size(max = 50, message = "Module key must not exceed 50 characters")
    private String moduleKey;

    /**
     * Unique page key within the module (e.g., "chat", "tickets", "dashboard")
     * Must be lowercase alphanumeric with underscores, max 100 characters
     */
    @NotBlank(message = "Page key is required")
    @Size(max = 100, message = "Page key must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Page key must be lowercase alphanumeric with underscores only")
    private String pageKey;

    /**
     * Human-readable page name
     */
    @NotBlank(message = "Page name is required")
    @Size(max = 200, message = "Page name must not exceed 200 characters")
    private String pageName;

    /**
     * Route path for this page (e.g., "/support/chat", "/support/tickets")
     */
    @Size(max = 500, message = "Route path must not exceed 500 characters")
    private String routePath;

    /**
     * Page description
     */
    private String description;
}
