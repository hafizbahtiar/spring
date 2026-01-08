package com.hafizbahtiar.spring.features.permissions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for registry import response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryImportResponse {

    /**
     * Whether import was successful
     */
    private Boolean success;

    /**
     * Number of modules imported
     */
    private Integer modulesImported;

    /**
     * Number of pages imported
     */
    private Integer pagesImported;

    /**
     * Number of components imported
     */
    private Integer componentsImported;

    /**
     * Number of modules skipped (due to conflicts)
     */
    private Integer modulesSkipped;

    /**
     * Number of pages skipped (due to conflicts)
     */
    private Integer pagesSkipped;

    /**
     * Number of components skipped (due to conflicts)
     */
    private Integer componentsSkipped;

    /**
     * Total number of items imported
     */
    private Integer totalImported;

    /**
     * Total number of items skipped
     */
    private Integer totalSkipped;

    /**
     * List of import errors
     */
    private List<ImportError> errors;

    /**
     * Import message
     */
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        /**
         * Resource type (MODULE, PAGE, COMPONENT)
         */
        private String resourceType;

        /**
         * Resource identifier
         */
        private String resourceIdentifier;

        /**
         * Error message
         */
        private String error;

        /**
         * Error type
         */
        private String errorType;
    }
}
