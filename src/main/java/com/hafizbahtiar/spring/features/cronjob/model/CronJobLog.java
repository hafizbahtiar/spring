package com.hafizbahtiar.spring.features.cronjob.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for cron job event logging.
 * Stores cron job-related events like creation, updates, deletions, enable/disable,
 * and manual executions for audit purposes.
 */
@Document(collection = "cron_job_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronJobLog {

    @Id
    private String id;

    /**
     * Cron job ID (from PostgreSQL)
     */
    @Indexed
    private Long cronJobId;

    /**
     * Cron job name
     */
    @Indexed
    private String jobName;

    /**
     * Type of cron job event
     * Values: JOB_CREATED, JOB_UPDATED, JOB_DELETED, JOB_ENABLED, JOB_DISABLED,
     * JOB_MANUAL_EXECUTION, JOB_SCHEDULED, JOB_UNSCHEDULED
     */
    @Indexed
    private String eventType;

    /**
     * User ID who performed the action
     */
    @Indexed
    private Long userId;

    /**
     * Timestamp when the event occurred
     */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * IP address of the client
     */
    private String ipAddress;

    /**
     * User agent string from the request
     */
    private String userAgent;

    /**
     * Session ID (if available)
     */
    @Indexed
    private String sessionId;

    /**
     * Request ID for tracing
     */
    @Indexed
    private String requestId;

    /**
     * Success status of the operation
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     */
    private String failureReason;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;

    /**
     * Additional metadata as flexible JSON structure
     */
    private Object metadata;
}
