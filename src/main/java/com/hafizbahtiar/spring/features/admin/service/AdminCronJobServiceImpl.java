package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.CronJobExecution;
import com.hafizbahtiar.spring.features.admin.dto.CronJobHistoryResponse;
import com.hafizbahtiar.spring.features.admin.dto.CronJobStatus;
import com.hafizbahtiar.spring.features.admin.model.CronJobExecutionLog;
import com.hafizbahtiar.spring.features.admin.repository.mongodb.CronJobExecutionLogRepository;
import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import com.hafizbahtiar.spring.features.cronjob.repository.CronJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AdminCronJobService for cron job management.
 * Reads from CronJob entity and MongoDB execution logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCronJobServiceImpl implements AdminCronJobService {

        private final CronJobExecutionLogRepository cronJobExecutionLogRepository;
        private final CronJobRepository cronJobRepository;

        @Override
        public List<CronJobStatus> getAllCronJobStatuses() {
                log.debug("Getting all cron job statuses");

                // Get all cron jobs from database
                List<CronJob> cronJobs = cronJobRepository.findAll();

                // Convert to CronJobStatus DTOs
                return cronJobs.stream()
                                .map(this::toCronJobStatus)
                                .collect(Collectors.toList());
        }

        @Override
        public CronJobStatus getCronJobStatus(String jobName) {
                log.debug("Getting cron job status for: {}", jobName);

                // Find cron job by name (case-insensitive)
                CronJob cronJob = cronJobRepository.findByNameIgnoreCase(jobName)
                                .orElse(null);

                if (cronJob == null) {
                        // If job not found in database, return status based on execution logs only
                        // (for backward compatibility with jobs that might not be in the database yet)
                        return getCronJobStatusFromLogs(jobName);
                }

                return toCronJobStatus(cronJob);
        }

        /**
         * Convert CronJob entity to CronJobStatus DTO
         */
        private CronJobStatus toCronJobStatus(CronJob cronJob) {
                // Get latest execution from MongoDB
                CronJobExecutionLog latestExecution = cronJobExecutionLogRepository
                                .findFirstByJobNameOrderByExecutedAtDesc(cronJob.getName());

                // Get execution counts
                long totalCount = cronJobExecutionLogRepository.countByJobName(cronJob.getName());
                long successCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(cronJob.getName(), true);
                long failureCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(cronJob.getName(), false);

                // Calculate next execution time
                LocalDateTime nextExecution = null;
                try {
                        CronExpression cron = CronExpression.parse(cronJob.getCronExpression());
                        nextExecution = cron.next(LocalDateTime.now());
                } catch (Exception e) {
                        log.debug("Could not calculate next execution time for job: {}", cronJob.getName(), e);
                }

                // Build status response
                CronJobStatus.CronJobStatusBuilder builder = CronJobStatus.builder()
                                .name(cronJob.getName())
                                .cronExpression(cronJob.getCronExpression())
                                .enabled(cronJob.isEnabled())
                                .executionCount(totalCount)
                                .successCount(successCount)
                                .failureCount(failureCount)
                                .nextExecution(nextExecution);

                if (latestExecution != null) {
                        builder.lastExecution(latestExecution.getExecutedAt())
                                        .lastDuration(latestExecution.getDuration())
                                        .lastError(latestExecution.getError());
                }

                return builder.build();
        }

        /**
         * Get cron job status from execution logs only (for backward compatibility)
         * Used when job is not found in the database
         */
        private CronJobStatus getCronJobStatusFromLogs(String jobName) {
                // Get latest execution from MongoDB
                CronJobExecutionLog latestExecution = cronJobExecutionLogRepository
                                .findFirstByJobNameOrderByExecutedAtDesc(jobName);

                // Get execution counts
                long totalCount = cronJobExecutionLogRepository.countByJobName(jobName);
                long successCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(jobName, true);
                long failureCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(jobName, false);

                // Build status response (without cron expression and enabled status)
                CronJobStatus.CronJobStatusBuilder builder = CronJobStatus.builder()
                                .name(jobName)
                                .cronExpression("") // Not available from logs
                                .enabled(false) // Unknown status
                                .executionCount(totalCount)
                                .successCount(successCount)
                                .failureCount(failureCount);

                if (latestExecution != null) {
                        builder.lastExecution(latestExecution.getExecutedAt())
                                        .lastDuration(latestExecution.getDuration())
                                        .lastError(latestExecution.getError());
                }

                return builder.build();
        }

        @Override
        public CronJobHistoryResponse getCronJobHistory(String jobName, Integer limit) {
                log.debug("Getting cron job history for: {}, limit: {}", jobName, limit);

                // Get execution history from MongoDB
                Pageable pageable = PageRequest.of(0, limit != null ? limit : 50);
                List<CronJobExecutionLog> logs = cronJobExecutionLogRepository
                                .findByJobNameOrderByExecutedAtDesc(jobName, pageable)
                                .getContent();

                long total = cronJobExecutionLogRepository.countByJobName(jobName);

                // Convert to DTOs
                List<CronJobExecution> executions = logs.stream()
                                .map(log -> CronJobExecution.builder()
                                                .jobName(log.getJobName())
                                                .executedAt(log.getExecutedAt())
                                                .success(log.getSuccess())
                                                .error(log.getError())
                                                .duration(log.getDuration())
                                                .build())
                                .collect(Collectors.toList());

                return CronJobHistoryResponse.builder()
                                .jobName(jobName)
                                .history(executions)
                                .total(total)
                                .build();
        }
}
