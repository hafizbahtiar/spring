package com.hafizbahtiar.spring.features.cronjob.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CronJob entity for storing cron job definitions.
 * Supports both application-level (Spring) and database-level (pg_cron) job types.
 */
@Entity
@Table(name = "cron_jobs", indexes = {
        @Index(name = "idx_cron_job_name", columnList = "name", unique = true),
        @Index(name = "idx_cron_job_enabled", columnList = "enabled"),
        @Index(name = "idx_cron_job_type", columnList = "job_type"),
        @Index(name = "idx_cron_job_enabled_type", columnList = "enabled, job_type")
})
@Getter
@Setter
@NoArgsConstructor
public class CronJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique job name (e.g., "session-cleanup", "daily-report")
     */
    @NotBlank(message = "Job name is required")
    @Size(min = 3, max = 100, message = "Job name must be between 3 and 100 characters")
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Job description (optional)
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Job type: APPLICATION or DATABASE
     */
    @NotNull(message = "Job type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    /**
     * Cron expression (e.g., "0 0 2 * * ?" for daily at 2 AM)
     * Uses Spring cron format: second minute hour day month day-of-week
     */
    @NotBlank(message = "Cron expression is required")
    @Pattern(regexp = "^[0-9*,\\-/\\s?]+$", message = "Invalid cron expression format")
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /**
     * Whether the job is enabled (scheduled)
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    /**
     * Job class and method for APPLICATION type jobs
     * Format: "com.example.Service.methodName" or "Service.methodName"
     * Example: "SessionCleanupService.cleanupExpiredSessions"
     */
    @Size(max = 255, message = "Job class must not exceed 255 characters")
    @Column(name = "job_class", length = 255)
    private String jobClass;

    /**
     * SQL script for DATABASE type jobs
     * Executed by PostgreSQL pg_cron extension
     */
    @Column(name = "sql_script", columnDefinition = "TEXT")
    private String sqlScript;

    /**
     * User who created this job
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Timestamp when job was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when job was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Constructor for creating a new cron job
     */
    public CronJob(String name, String description, JobType jobType, String cronExpression, User createdBy) {
        this.name = name;
        this.description = description;
        this.jobType = jobType;
        this.cronExpression = cronExpression;
        this.createdBy = createdBy;
        this.enabled = false;
    }

    /**
     * Business method to check if job is enabled
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }

    /**
     * Business method to enable the job
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Business method to disable the job
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Business method to validate job configuration
     * APPLICATION jobs must have jobClass, DATABASE jobs must have sqlScript
     */
    public boolean isValid() {
        if (jobType == JobType.APPLICATION) {
            return jobClass != null && !jobClass.trim().isEmpty();
        } else if (jobType == JobType.DATABASE) {
            return sqlScript != null && !sqlScript.trim().isEmpty();
        }
        return false;
    }
}
