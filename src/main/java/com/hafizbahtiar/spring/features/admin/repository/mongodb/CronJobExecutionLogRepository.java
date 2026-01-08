package com.hafizbahtiar.spring.features.admin.repository.mongodb;

import com.hafizbahtiar.spring.features.admin.model.CronJobExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * MongoDB repository for cron job execution logs.
 */
@Repository
public interface CronJobExecutionLogRepository extends MongoRepository<CronJobExecutionLog, String> {

    /**
     * Find all executions for a specific job, ordered by execution time descending.
     *
     * @param jobName  Name of the cron job
     * @param pageable Pagination information
     * @return Page of execution logs
     */
    Page<CronJobExecutionLog> findByJobNameOrderByExecutedAtDesc(String jobName, Pageable pageable);

    /**
     * Find the latest execution for a specific job.
     *
     * @param jobName Name of the cron job
     * @return Latest execution log, or null if none exists
     */
    CronJobExecutionLog findFirstByJobNameOrderByExecutedAtDesc(String jobName);

    /**
     * Count executions for a specific job.
     *
     * @param jobName Name of the cron job
     * @return Total count of executions
     */
    long countByJobName(String jobName);

    /**
     * Count successful executions for a specific job.
     *
     * @param jobName Name of the cron job
     * @return Count of successful executions
     */
    long countByJobNameAndSuccess(String jobName, Boolean success);

    /**
     * Delete old execution logs before a certain date.
     *
     * @param beforeDate Date before which logs should be deleted
     * @return Number of deleted logs
     */
    long deleteByExecutedAtBefore(LocalDateTime beforeDate);
}
