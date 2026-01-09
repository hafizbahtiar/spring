package com.hafizbahtiar.spring.features.cronjob.dto;

import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new cron job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCronJobRequest {

    /**
     * Unique job name (e.g., "session-cleanup", "daily-report")
     */
    @NotBlank(message = "Job name is required")
    @Size(min = 3, max = 100, message = "Job name must be between 3 and 100 characters")
    private String name;

    /**
     * Job description (optional)
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Job type: APPLICATION or DATABASE
     */
    @NotNull(message = "Job type is required")
    private JobType jobType;

    /**
     * Cron expression (e.g., "0 0 2 * * ?" for daily at 2 AM)
     * Uses Spring cron format: second minute hour day month day-of-week
     */
    @NotBlank(message = "Cron expression is required")
    @Pattern(regexp = "^[0-9*,\\-/\\s?]+$", message = "Invalid cron expression format")
    private String cronExpression;

    /**
     * Job class and method for APPLICATION type jobs
     * Format: "ServiceName.methodName" or "com.package.ServiceName.methodName"
     * Example: "SessionCleanupService.cleanupExpiredSessions"
     */
    @Size(max = 255, message = "Job class must not exceed 255 characters")
    private String jobClass;

    /**
     * SQL script for DATABASE type jobs
     * Executed by PostgreSQL pg_cron extension
     */
    private String sqlScript;

    /**
     * Whether the job should be enabled immediately after creation
     */
    private Boolean enabled = false;
}
