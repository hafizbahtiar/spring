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
 * DTO for bulk updating permission pages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdatePageRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageUpdate {
        /**
         * Page ID to update
         */
        @NotNull(message = "Page ID is required")
        private Long id;

        /**
         * Update request
         */
        @NotNull(message = "Update request is required")
        @Valid
        private UpdatePageRequest request;
    }

    /**
     * List of page updates
     */
    @NotEmpty(message = "Page updates list cannot be empty")
    @Size(max = 100, message = "Cannot update more than 100 pages at once")
    @Valid
    private List<PageUpdate> pages;
}
