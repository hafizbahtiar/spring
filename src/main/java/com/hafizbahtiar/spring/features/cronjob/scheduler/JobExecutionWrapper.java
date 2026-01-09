package com.hafizbahtiar.spring.features.cronjob.scheduler;

import com.hafizbahtiar.spring.features.admin.model.CronJobExecutionLog;
import com.hafizbahtiar.spring.features.admin.repository.mongodb.CronJobExecutionLogRepository;
import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Wrapper for cron job execution that handles logging and error handling.
 * Logs execution start/end to MongoDB and handles exceptions gracefully.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobExecutionWrapper {

    private final CronJobExecutionLogRepository cronJobExecutionLogRepository;

    /**
     * Execute a job with logging and error handling.
     *
     * @param cronJob  The cron job to execute
     * @param executor Runnable that executes the actual job logic
     */
    public void executeWithLogging(CronJob cronJob, Runnable executor) {
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();
        boolean success = false;
        String error = null;

        log.info("Starting execution of cron job: {}", cronJob.getName());

        try {
            executor.run();
            success = true;
            long duration = System.currentTimeMillis() - startMillis;
            log.info("Successfully executed cron job: {} in {} ms", cronJob.getName(), duration);

        } catch (Exception e) {
            success = false;
            error = e.getMessage();
            long duration = System.currentTimeMillis() - startMillis;
            log.error("Failed to execute cron job: {} after {} ms", cronJob.getName(), duration, e);

        } finally {
            // Log execution to MongoDB
            try {
                long duration = System.currentTimeMillis() - startMillis;
                CronJobExecutionLog executionLog = CronJobExecutionLog.builder()
                        .jobName(cronJob.getName())
                        .executedAt(startTime)
                        .success(success)
                        .error(error)
                        .duration(duration)
                        .createdAt(LocalDateTime.now())
                        .build();

                cronJobExecutionLogRepository.save(executionLog);
                log.debug("Logged execution for job: {} to MongoDB", cronJob.getName());

            } catch (Exception e) {
                log.error("Failed to log execution for job: {} to MongoDB", cronJob.getName(), e);
                // Don't throw - logging failure shouldn't break job execution
            }
        }
    }
}
