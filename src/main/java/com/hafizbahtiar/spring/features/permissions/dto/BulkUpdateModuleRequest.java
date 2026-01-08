package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk updating permission modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateModuleRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleUpdate {
        /**
         * Module ID to update
         */
        @NotNull(message = "Module ID is required")
        private Long id;

        /**
         * Update request
         */
        @NotNull(message = "Update request is required")
        @Valid
        private UpdateModuleRequest request;
    }

    /**
     * List of module updates
     */
    @NotEmpty(message = "Module updates list cannot be empty")
    @Size(max = 100, message = "Cannot update more than 100 modules at once")
    @Valid
    private List<ModuleUpdate> modules;
}
