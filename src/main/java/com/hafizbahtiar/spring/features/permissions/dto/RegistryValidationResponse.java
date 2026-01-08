package com.hafizbahtiar.spring.features.permissions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for registry validation response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryValidationResponse {

    /**
     * Whether the registry is valid
     */
    private Boolean isValid;

    /**
     * Total number of issues found
     */
    private Integer issueCount;

    /**
     * List of validation issues
     */
    private List<ValidationIssue> issues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssue {
        /**
         * Issue type (ORPHANED_PAGE, ORPHANED_COMPONENT, DUPLICATE_ROUTE, etc.)
         */
        private String type;

        /**
         * Severity level (ERROR, WARNING, INFO)
         */
        private String severity;

        /**
         * Issue message
         */
        private String message;

        /**
         * Affected resource type (MODULE, PAGE, COMPONENT)
         */
        private String resourceType;

        /**
         * Affected resource ID
         */
        private Long resourceId;

        /**
         * Affected resource identifier (key)
         */
        private String resourceIdentifier;

        /**
         * Additional details about the issue
         */
        private String details;
    }
}
