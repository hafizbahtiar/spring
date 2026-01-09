package com.hafizbahtiar.spring.features.cronjob.dto;

import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing cron job.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCronJobRequest {

    /**
     * Job name (optional, for renaming)
     */
    @Size(min = 3, max = 100, message = "Job name must be between 3 and 100 characters")
    private String name;

    /**
     * Job description (optional)
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Job type (optional, cannot be changed after creation)
     */
    private JobType jobType;

    /**
     * Cron expression (optional, for rescheduling)
     */
    @Pattern(regexp = "^[0-9*,\\-/\\s?]+$", message = "Invalid cron expression format")
    private String cronExpression;

    /**
     * Job class and method for APPLICATION type jobs (optional)
     */
    @Size(max = 255, message = "Job class must not exceed 255 characters")
    private String jobClass;

    /**
     * SQL script for DATABASE type jobs (optional)
     */
    private String sqlScript;

    /**
     * Whether the job is enabled (optional)
     */
    private Boolean enabled;
}
