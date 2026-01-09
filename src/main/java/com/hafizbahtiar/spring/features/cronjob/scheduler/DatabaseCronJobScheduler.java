package com.hafizbahtiar.spring.features.cronjob.scheduler;

import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Scheduler for database-level cron jobs using PostgreSQL pg_cron extension.
 * 
 * Note: This scheduler requires the pg_cron extension to be installed in
 * PostgreSQL.
 * If the extension is not available, database jobs will be disabled.
 * 
 * pg_cron job format:
 * - Job name: Must be unique across all pg_cron jobs
 * - Schedule: Uses standard cron format (minute hour day month day-of-week)
 * - Command: SQL script to execute
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseCronJobScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Value("${cronjob.database.enabled:true}")
    private boolean databaseJobsEnabled;

    private boolean pgCronAvailable = false;

    /**
     * Check if pg_cron extension is available.
     * Called on startup to verify extension installation.
     */
    @PostConstruct
    public void checkPgCronAvailability() {
        if (!databaseJobsEnabled) {
            log.info("Database cron jobs are disabled via configuration");
            return;
        }

        try {
            // Check if pg_cron extension exists
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT EXISTS(SELECT 1 FROM pg_extension WHERE extname = 'pg_cron') as exists");

            boolean exists = (Boolean) results.get(0).get("exists");

            if (exists) {
                pgCronAvailable = true;
                log.info("pg_cron extension is available. Database cron jobs are enabled.");
            } else {
                pgCronAvailable = false;
                log.warn("pg_cron extension is not installed. Database cron jobs will be disabled.");
                log.warn("To enable database cron jobs, install pg_cron: CREATE EXTENSION IF NOT EXISTS pg_cron;");
            }
        } catch (Exception e) {
            pgCronAvailable = false;
            log.error("Failed to check pg_cron extension availability", e);
            log.warn("Database cron jobs will be disabled due to check failure");
        }
    }

    /**
     * Schedule a database cron job using pg_cron.
     *
     * @param cronJob The cron job to schedule
     * @throws IllegalArgumentException if the job cannot be scheduled
     */
    public void scheduleDatabaseJob(CronJob cronJob) {
        if (cronJob.getJobType() != JobType.DATABASE) {
            throw new IllegalArgumentException(
                    "DatabaseCronJobScheduler only handles DATABASE type jobs. Job: " + cronJob.getName());
        }

        if (!pgCronAvailable) {
            throw new IllegalStateException(
                    "pg_cron extension is not available. Cannot schedule database job: " + cronJob.getName());
        }

        if (!cronJob.isValid()) {
            throw new IllegalArgumentException("Job configuration is invalid. Job: " + cronJob.getName());
        }

        if (cronJob.getSqlScript() == null || cronJob.getSqlScript().trim().isEmpty()) {
            throw new IllegalArgumentException("SQL script is required for database jobs. Job: " + cronJob.getName());
        }

        // Validate SQL script (basic check - no dangerous operations)
        String sqlScript = cronJob.getSqlScript().trim();
        if (sqlScript.toLowerCase().contains("drop database") ||
                sqlScript.toLowerCase().contains("drop schema") ||
                sqlScript.toLowerCase().contains("truncate")) {
            log.warn("SQL script contains potentially dangerous operations. Job: {}", cronJob.getName());
        }

        try {
            // Convert Spring cron expression to pg_cron format
            // Spring: "0 0 2 * * ?" (second minute hour day month day-of-week)
            // pg_cron: "0 2 * * *" (minute hour day month day-of-week)
            String pgCronSchedule = convertToPgCronFormat(cronJob.getCronExpression());

            // Check if job already exists
            if (pgCronJobExists(cronJob.getName())) {
                // Update existing job
                unscheduleDatabaseJob(cronJob.getName());
            }

            // Schedule the job using pg_cron.schedule
            // pg_cron.schedule(job_name, schedule, command)
            String scheduleSql = String.format(
                    "SELECT cron.schedule('%s', '%s', '%s')",
                    sanitizeJobName(cronJob.getName()),
                    pgCronSchedule,
                    sqlScript.replace("'", "''") // Escape single quotes
            );

            jdbcTemplate.execute(scheduleSql);
            log.info("Scheduled database job: {} with schedule: {}", cronJob.getName(), pgCronSchedule);

        } catch (Exception e) {
            log.error("Failed to schedule database job: {}", cronJob.getName(), e);
            throw new RuntimeException(
                    "Failed to schedule database job: " + cronJob.getName() + ". Error: " + e.getMessage(), e);
        }
    }

    /**
     * Unschedule a database cron job.
     *
     * @param jobName The name of the job to unschedule
     */
    public void unscheduleDatabaseJob(String jobName) {
        if (!pgCronAvailable) {
            log.warn("pg_cron extension is not available. Cannot unschedule database job: {}", jobName);
            return;
        }

        try {
            // Check if job exists
            if (!pgCronJobExists(jobName)) {
                log.debug("Database job does not exist: {}", jobName);
                return;
            }

            // Unschedule the job using pg_cron.unschedule
            // pg_cron.unschedule(job_name)
            String unscheduleSql = String.format(
                    "SELECT cron.unschedule('%s')",
                    sanitizeJobName(jobName));

            jdbcTemplate.execute(unscheduleSql);
            log.info("Unscheduled database job: {}", jobName);

        } catch (Exception e) {
            log.error("Failed to unschedule database job: {}", jobName, e);
            throw new RuntimeException("Failed to unschedule database job: " + jobName + ". Error: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Update a database cron job (reschedule with new configuration).
     *
     * @param cronJob The cron job to update
     */
    public void updateDatabaseJob(CronJob cronJob) {
        log.debug("Updating database job: {}", cronJob.getName());
        scheduleDatabaseJob(cronJob); // Reschedule with new config
    }

    /**
     * Check if a pg_cron job exists.
     *
     * @param jobName The name of the job
     * @return true if exists, false otherwise
     */
    private boolean pgCronJobExists(String jobName) {
        try {
            String checkSql = String.format(
                    "SELECT COUNT(*) FROM cron.job WHERE jobname = '%s'",
                    sanitizeJobName(jobName));
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Error checking if pg_cron job exists: {}", jobName, e);
            return false;
        }
    }

    /**
     * Convert Spring cron expression to pg_cron format.
     * Spring: "0 0 2 * * ?" (second minute hour day month day-of-week)
     * pg_cron: "0 2 * * *" (minute hour day month day-of-week)
     */
    private String convertToPgCronFormat(String springCronExpression) {
        // Split Spring cron: second minute hour day month day-of-week
        String[] parts = springCronExpression.trim().split("\\s+");

        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid cron expression format: " + springCronExpression);
        }

        // Extract: minute, hour, day, month, day-of-week
        // Skip second (index 0) and use minute (1), hour (2), day (3), month (4),
        // day-of-week (5)
        // Convert ? to * for day-of-week if needed
        String minute = parts[1];
        String hour = parts[2];
        String day = parts[3];
        String month = parts[4];
        String dayOfWeek = parts[5].equals("?") ? "*" : parts[5];

        return String.format("%s %s %s %s %s", minute, hour, day, month, dayOfWeek);
    }

    /**
     * Sanitize job name for SQL injection prevention.
     * Only allows alphanumeric, underscore, and hyphen.
     */
    private String sanitizeJobName(String jobName) {
        if (jobName == null) {
            throw new IllegalArgumentException("Job name cannot be null");
        }
        // Remove any characters that could be used for SQL injection
        return jobName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Check if pg_cron is available.
     *
     * @return true if available, false otherwise
     */
    public boolean isPgCronAvailable() {
        return pgCronAvailable && databaseJobsEnabled;
    }
}
