package com.hafizbahtiar.spring.features.cronjob.exception;

/**
 * General exception for cron job-related errors.
 */
public class CronJobException extends RuntimeException {

    public CronJobException(String message) {
        super(message);
    }

    public CronJobException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Thrown when a cron job with the given name already exists.
     */
    public static CronJobException nameAlreadyExists(String name) {
        return new CronJobException("Cron job with name already exists: " + name);
    }

    /**
     * Thrown when a cron job is not found.
     */
    public static CronJobException notFound(Long id) {
        return new CronJobException("Cron job not found with ID: " + id);
    }

    /**
     * Thrown when a cron job is not found by name.
     */
    public static CronJobException notFoundByName(String name) {
        return new CronJobException("Cron job not found with name: " + name);
    }

    /**
     * Thrown when cron expression is invalid.
     */
    public static CronJobException invalidCronExpression(String cronExpression, String reason) {
        return new CronJobException("Invalid cron expression: " + cronExpression + ". Reason: " + reason);
    }

    /**
     * Thrown when job configuration is invalid.
     */
    public static CronJobException invalidConfiguration(String message) {
        return new CronJobException("Invalid job configuration: " + message);
    }

    /**
     * Thrown when job class or method is not found.
     */
    public static CronJobException jobClassNotFound(String jobClass) {
        return new CronJobException("Job class or method not found: " + jobClass);
    }

    /**
     * Thrown when SQL script is invalid or missing.
     */
    public static CronJobException invalidSqlScript(String reason) {
        return new CronJobException("Invalid SQL script: " + reason);
    }

    /**
     * Thrown when pg_cron extension is not available.
     */
    public static CronJobException pgCronNotAvailable() {
        return new CronJobException("pg_cron extension is not available. Database jobs cannot be scheduled.");
    }
}
