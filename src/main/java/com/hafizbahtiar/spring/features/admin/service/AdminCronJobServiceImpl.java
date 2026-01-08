package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.CronJobExecution;
import com.hafizbahtiar.spring.features.admin.dto.CronJobHistoryResponse;
import com.hafizbahtiar.spring.features.admin.dto.CronJobStatus;
import com.hafizbahtiar.spring.features.admin.model.CronJobExecutionLog;
import com.hafizbahtiar.spring.features.admin.repository.mongodb.CronJobExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AdminCronJobService for cron job management.
 * 
 * Note: Currently returns placeholder data as no scheduled tasks are
 * implemented.
 * This can be extended to integrate with Spring's @Scheduled tasks or other
 * scheduling systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCronJobServiceImpl implements AdminCronJobService {

    private final CronJobExecutionLogRepository cronJobExecutionLogRepository;

    @Override
    public List<CronJobStatus> getAllCronJobStatuses() {
        log.debug("Getting all cron job statuses");

        // Return empty list as no scheduled tasks are implemented
        // In the future, this would discover and return status for all @Scheduled
        // methods

        return Collections.emptyList();
    }

    @Override
    public CronJobStatus getCronJobStatus(String jobName) {
        log.debug("Getting cron job status for: {}", jobName);

        // Get latest execution from MongoDB
        CronJobExecutionLog latestExecution = cronJobExecutionLogRepository
                .findFirstByJobNameOrderByExecutedAtDesc(jobName);

        // Get execution counts
        long totalCount = cronJobExecutionLogRepository.countByJobName(jobName);
        long successCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(jobName, true);
        long failureCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(jobName, false);

        // Build status response
        CronJobStatus.CronJobStatusBuilder builder = CronJobStatus.builder()
                .name(jobName)
                .cronExpression("") // Would be extracted from @Scheduled annotation
                .enabled(false) // Would be tracked separately
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
