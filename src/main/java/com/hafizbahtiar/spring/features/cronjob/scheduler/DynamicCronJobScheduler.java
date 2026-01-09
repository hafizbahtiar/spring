package com.hafizbahtiar.spring.features.cronjob.scheduler;

import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import com.hafizbahtiar.spring.features.cronjob.executor.ApplicationJobExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Dynamic scheduler for application-level cron jobs.
 * Uses Spring's ThreadPoolTaskScheduler to schedule jobs dynamically.
 * 
 * Note: This scheduler only handles APPLICATION type jobs.
 * DATABASE type jobs are handled by DatabaseCronJobScheduler.
 */
@Component
@Slf4j
public class DynamicCronJobScheduler {

    private final TaskScheduler taskScheduler;
    private final ApplicationJobExecutor applicationJobExecutor;
    private final JobExecutionWrapper jobExecutionWrapper;

    public DynamicCronJobScheduler(
            @Qualifier("cronJobTaskScheduler") TaskScheduler taskScheduler,
            ApplicationJobExecutor applicationJobExecutor,
            JobExecutionWrapper jobExecutionWrapper) {
        this.taskScheduler = taskScheduler;
        this.applicationJobExecutor = applicationJobExecutor;
        this.jobExecutionWrapper = jobExecutionWrapper;
    }

    /**
     * Map of scheduled jobs: jobName -> ScheduledFuture
     */
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    /**
     * Schedule a cron job dynamically.
     *
     * @param cronJob The cron job to schedule
     * @throws IllegalArgumentException if the job cannot be scheduled
     */
    public void scheduleJob(CronJob cronJob) {
        if (cronJob.getJobType() != JobType.APPLICATION) {
            throw new IllegalArgumentException(
                    "DynamicCronJobScheduler only handles APPLICATION type jobs. Job: " + cronJob.getName());
        }

        if (!cronJob.isValid()) {
            throw new IllegalArgumentException("Job configuration is invalid. Job: " + cronJob.getName());
        }

        // Validate cron expression
        try {
            CronExpression.parse(cronJob.getCronExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid cron expression: " + cronJob.getCronExpression() + " for job: " + cronJob.getName(), e);
        }

        // Validate job class and method
        if (!applicationJobExecutor.validate(cronJob)) {
            throw new IllegalArgumentException(
                    "Job class or method not found. Job: " + cronJob.getName() + ", Class: " + cronJob.getJobClass());
        }

        // Unschedule existing job if it exists
        unscheduleJob(cronJob.getName());

        // Create cron trigger
        CronTrigger trigger = new CronTrigger(cronJob.getCronExpression(), ZoneId.systemDefault());

        // Schedule the job
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            jobExecutionWrapper.executeWithLogging(cronJob, () -> {
                try {
                    applicationJobExecutor.execute(cronJob);
                } catch (ApplicationJobExecutor.JobExecutionException e) {
                    throw new RuntimeException("Job execution failed: " + e.getMessage(), e);
                }
            });
        }, trigger);

        scheduledJobs.put(cronJob.getName(), future);
        log.info("Scheduled application job: {} with cron expression: {}", cronJob.getName(),
                cronJob.getCronExpression());
    }

    /**
     * Unschedule a cron job.
     *
     * @param jobName The name of the job to unschedule
     */
    public void unscheduleJob(String jobName) {
        ScheduledFuture<?> future = scheduledJobs.remove(jobName);
        if (future != null) {
            future.cancel(false);
            log.info("Unscheduled application job: {}", jobName);
        }
    }

    /**
     * Reschedule a cron job (update existing schedule).
     * This is equivalent to unscheduling and then scheduling again.
     *
     * @param cronJob The cron job to reschedule
     */
    public void rescheduleJob(CronJob cronJob) {
        log.debug("Rescheduling application job: {}", cronJob.getName());
        scheduleJob(cronJob);
    }

    /**
     * Load and schedule all enabled application jobs.
     * This is called on application startup.
     *
     * @param enabledJobs List of enabled cron jobs to schedule
     */
    public void loadAllEnabledJobs(java.util.List<CronJob> enabledJobs) {
        log.info("Loading {} enabled cron jobs for scheduling", enabledJobs.size());

        int scheduledCount = 0;
        int skippedCount = 0;

        for (CronJob job : enabledJobs) {
            if (job.getJobType() == JobType.APPLICATION) {
                try {
                    scheduleJob(job);
                    scheduledCount++;
                } catch (Exception e) {
                    log.error("Failed to schedule job: {}", job.getName(), e);
                    skippedCount++;
                }
            } else {
                log.debug("Skipping non-application job: {} (type: {})", job.getName(), job.getJobType());
                skippedCount++;
            }
        }

        log.info("Cron job loading completed: {} scheduled, {} skipped", scheduledCount, skippedCount);
    }

    /**
     * Check if a job is currently scheduled.
     *
     * @param jobName The name of the job
     * @return true if scheduled, false otherwise
     */
    public boolean isScheduled(String jobName) {
        ScheduledFuture<?> future = scheduledJobs.get(jobName);
        return future != null && !future.isCancelled();
    }

    /**
     * Get the number of currently scheduled jobs.
     *
     * @return Number of scheduled jobs
     */
    public int getScheduledJobCount() {
        return scheduledJobs.size();
    }

    /**
     * Unschedule all jobs on shutdown.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down DynamicCronJobScheduler, unscheduling {} jobs", scheduledJobs.size());
        for (String jobName : scheduledJobs.keySet()) {
            unscheduleJob(jobName);
        }
        scheduledJobs.clear();
    }
}
