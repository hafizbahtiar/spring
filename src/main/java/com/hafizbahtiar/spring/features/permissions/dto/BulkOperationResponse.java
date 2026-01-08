package com.hafizbahtiar.spring.features.permissions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk operation responses.
 * Supports partial success/failure scenarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResponse<T> {

    /**
     * Total number of items processed
     */
    private Integer totalCount;

    /**
     * Number of successful operations
     */
    private Integer successCount;

    /**
     * Number of failed operations
     */
    private Integer failureCount;

    /**
     * List of successfully created/updated items
     */
    private List<T> successfulItems;

    /**
     * List of operation failures
     */
    private List<OperationFailure> failures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationFailure {
        /**
         * Index of the failed item in the original request
         */
        private Integer index;

        /**
         * Resource identifier (key, ID, etc.)
         */
        private String resourceIdentifier;

        /**
         * Error message
         */
        private String error;

        /**
         * Error code or type
         */
        private String errorType;
    }
}
