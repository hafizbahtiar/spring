package com.hafizbahtiar.spring.features.permissions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for registry cleanup response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryCleanupResponse {

    /**
     * Whether cleanup was successful
     */
    private Boolean success;

    /**
     * Number of orphaned pages removed
     */
    private Integer orphanedPagesRemoved;

    /**
     * Number of orphaned components removed
     */
    private Integer orphanedComponentsRemoved;

    /**
     * Total number of records removed
     */
    private Integer totalRemoved;

    /**
     * List of removed resource IDs
     */
    private List<RemovedResource> removedResources;

    /**
     * Cleanup message
     */
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemovedResource {
        /**
         * Resource type (PAGE, COMPONENT)
         */
        private String resourceType;

        /**
         * Resource ID
         */
        private Long resourceId;

        /**
         * Resource identifier (key)
         */
        private String resourceIdentifier;

        /**
         * Reason for removal
         */
        private String reason;
    }
}
