package com.hafizbahtiar.spring.features.permissions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for registry health check response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryHealthResponse {

    /**
     * Overall health status (HEALTHY, DEGRADED, UNHEALTHY)
     */
    private String status;

    /**
     * Total number of modules
     */
    private Long moduleCount;

    /**
     * Total number of pages
     */
    private Long pageCount;

    /**
     * Total number of components
     */
    private Long componentCount;

    /**
     * Number of orphaned pages (pages without valid modules)
     */
    private Long orphanedPageCount;

    /**
     * Number of orphaned components (components without valid pages)
     */
    private Long orphanedComponentCount;

    /**
     * Number of duplicate route paths
     */
    private Long duplicateRouteCount;

    /**
     * Health message
     */
    private String message;

    /**
     * Timestamp of health check
     */
    private java.time.LocalDateTime checkedAt;
}
