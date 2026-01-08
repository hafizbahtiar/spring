package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for registry import request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryImportRequest {

    /**
     * Conflict resolution strategy: SKIP, OVERWRITE, MERGE
     */
    public enum ConflictResolution {
        SKIP,      // Skip items that already exist
        OVERWRITE, // Overwrite existing items
        MERGE      // Merge with existing items (update only provided fields)
    }

    /**
     * Import data
     */
    @NotNull(message = "Import data is required")
    @Valid
    private RegistryData data;

    /**
     * Conflict resolution strategy
     */
    @Builder.Default
    private ConflictResolution conflictResolution = ConflictResolution.SKIP;

    /**
     * Whether to validate before importing
     */
    @Builder.Default
    private Boolean validateBeforeImport = true;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistryData {
        @NotEmpty(message = "Modules list cannot be empty")
        @Valid
        private List<CreateModuleRequest> modules;

        @NotEmpty(message = "Pages list cannot be empty")
        @Valid
        private List<CreatePageRequest> pages;

        @NotEmpty(message = "Components list cannot be empty")
        @Valid
        private List<CreateComponentRequest> components;
    }
}

