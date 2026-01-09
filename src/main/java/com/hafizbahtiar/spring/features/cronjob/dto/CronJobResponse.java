package com.hafizbahtiar.spring.features.cronjob.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for cron job details.
 * Includes execution statistics from MongoDB logs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronJobResponse {

    private Long id;
    private String name;
    private String description;
    private JobType jobType;
    private String cronExpression;
    private Boolean enabled;
    private String jobClass;
    private String sqlScript;
    private Long createdBy;
    private String createdByName; // Optional: creator's name

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Execution statistics (from MongoDB logs)
     */
    private Long executionCount;
    private Long successCount;
    private Long failureCount;

    /**
     * Last execution information
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastExecution;
    private Long lastDuration; // in milliseconds
    private String lastError;

    /**
     * Next scheduled execution time (calculated from cron expression)
     * This is optional and may not be available for all job types
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextExecution;
}
