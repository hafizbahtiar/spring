package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a permission page.
 * All fields are optional for partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePageRequest {

    /**
     * Human-readable page name
     */
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
