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
 * DTO for bulk updating permission components.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateComponentRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentUpdate {
        /**
         * Component ID to update
         */
        @NotNull(message = "Component ID is required")
        private Long id;

        /**
         * Update request
         */
        @NotNull(message = "Update request is required")
        @Valid
        private UpdateComponentRequest request;
    }

    /**
     * List of component updates
     */
    @NotEmpty(message = "Component updates list cannot be empty")
    @Size(max = 100, message = "Cannot update more than 100 components at once")
    @Valid
    private List<ComponentUpdate> components;
}
