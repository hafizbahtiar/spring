package com.hafizbahtiar.spring.features.cronjob.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for manually executing a cron job.
 * Currently only requires jobId, but can be extended in the future
 * to support additional execution parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteJobRequest {

    /**
     * Cron job ID to execute
     */
    @NotNull(message = "Job ID is required")
    private Long jobId;

    /**
     * Optional: Force execution even if job is disabled
     * Default: false (only enabled jobs can be executed)
     */
    private Boolean forceExecution = false;

    /**
     * Optional: Execution timeout in seconds
     * Default: null (no timeout)
     */
    private Integer timeoutSeconds;
}
