package com.hafizbahtiar.spring.features.cronjob.config;

import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import com.hafizbahtiar.spring.features.cronjob.repository.CronJobRepository;
import com.hafizbahtiar.spring.features.cronjob.scheduler.DatabaseCronJobScheduler;
import com.hafizbahtiar.spring.features.cronjob.scheduler.DynamicCronJobScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Initializes cron jobs on application startup.
 * Loads all enabled jobs from database and schedules them.
 * 
 * Order: 10 (runs after DataInitializer and PermissionRegistryInitializer,
 * but before other application components)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class CronJobInitializer implements CommandLineRunner {

    private final CronJobRepository cronJobRepository;
    private final DynamicCronJobScheduler dynamicCronJobScheduler;
    private final DatabaseCronJobScheduler databaseCronJobScheduler;

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        log.info("Starting cron job initialization...");

        // Load all enabled cron jobs from database
        List<CronJob> enabledJobs = cronJobRepository.findByEnabledTrue();

        if (enabledJobs.isEmpty()) {
            log.info("No enabled cron jobs found. Skipping cron job initialization.");
            return;
        }

        log.info("Found {} enabled cron job(s) to schedule", enabledJobs.size());

        // Group jobs by type
        Map<JobType, List<CronJob>> jobsByType = enabledJobs.stream()
                .collect(Collectors.groupingBy(CronJob::getJobType));

        // Schedule application jobs
        List<CronJob> applicationJobs = jobsByType.getOrDefault(JobType.APPLICATION, List.of());
        if (!applicationJobs.isEmpty()) {
            log.info("Scheduling {} application job(s)...", applicationJobs.size());
            dynamicCronJobScheduler.loadAllEnabledJobs(applicationJobs);
        }

        // Schedule database jobs
        List<CronJob> databaseJobs = jobsByType.getOrDefault(JobType.DATABASE, List.of());
        if (!databaseJobs.isEmpty()) {
            // Verify pg_cron availability
            if (!databaseCronJobScheduler.isPgCronAvailable()) {
                log.error("Cannot schedule {} database job(s): pg_cron extension is not available",
                        databaseJobs.size());
                log.error("Please install pg_cron extension: CREATE EXTENSION IF NOT EXISTS pg_cron;");
            } else {
                log.info("Scheduling {} database job(s)...", databaseJobs.size());
                scheduleDatabaseJobs(databaseJobs);
            }
        }

        // Log initialization summary
        logInitializationSummary(enabledJobs, applicationJobs.size(), databaseJobs.size());
    }

    /**
     * Schedule database jobs
     */
    private void scheduleDatabaseJobs(List<CronJob> databaseJobs) {
        int scheduledCount = 0;
        int skippedCount = 0;

        for (CronJob job : databaseJobs) {
            try {
                if (!job.isValid()) {
                    log.warn("Skipping invalid database job: {} (missing SQL script)", job.getName());
                    skippedCount++;
                    continue;
                }

                databaseCronJobScheduler.scheduleDatabaseJob(job);
                scheduledCount++;
            } catch (Exception e) {
                log.error("Failed to schedule database job: {}", job.getName(), e);
                skippedCount++;
            }
        }

        log.info("Database jobs scheduling completed: {} scheduled, {} skipped", scheduledCount, skippedCount);
    }

    /**
     * Log initialization summary
     */
    private void logInitializationSummary(List<CronJob> allJobs, int applicationCount, int databaseCount) {
        log.info("========================================");
        log.info("Cron Job Initialization Summary");
        log.info("========================================");
        log.info("Total enabled jobs: {}", allJobs.size());
        log.info("  - Application jobs: {}", applicationCount);
        log.info("  - Database jobs: {}", databaseCount);
        log.info("");

        // Log individual jobs
        if (!allJobs.isEmpty()) {
            log.info("Scheduled jobs:");
            for (CronJob job : allJobs) {
                log.info("  - {} ({}) - {}", job.getName(), job.getJobType(), job.getCronExpression());
            }
        }

        log.info("========================================");
        log.info("Cron job initialization completed.");
        log.info("========================================");
    }
}
