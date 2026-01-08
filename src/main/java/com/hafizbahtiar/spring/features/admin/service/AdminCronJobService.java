package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.CronJobHistoryResponse;
import com.hafizbahtiar.spring.features.admin.dto.CronJobStatus;

import java.util.List;

/**
 * Service interface for admin cron job management.
 */
public interface AdminCronJobService {

    /**
     * Get status for all cron jobs.
     *
     * @return List of cron job statuses
     */
    List<CronJobStatus> getAllCronJobStatuses();

    /**
     * Get status for a specific cron job.
     *
     * @param jobName Name of the cron job
     * @return CronJobStatus for the specified job
     */
    CronJobStatus getCronJobStatus(String jobName);

    /**
     * Get execution history for a specific cron job.
     *
     * @param jobName Name of the cron job
     * @param limit   Maximum number of executions to return
     * @return CronJobHistoryResponse with execution history
     */
    CronJobHistoryResponse getCronJobHistory(String jobName, Integer limit);
}
