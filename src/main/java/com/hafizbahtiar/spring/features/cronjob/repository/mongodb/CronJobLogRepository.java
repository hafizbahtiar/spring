package com.hafizbahtiar.spring.features.cronjob.repository.mongodb;

import com.hafizbahtiar.spring.features.cronjob.model.CronJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * MongoDB repository for cron job event logs.
 */
@Repository
public interface CronJobLogRepository extends MongoRepository<CronJobLog, String> {

    /**
     * Find all logs for a specific cron job, ordered by timestamp descending.
     *
     * @param cronJobId Cron job ID
     * @param pageable  Pagination information
     * @return Page of cron job logs
     */
    Page<CronJobLog> findByCronJobIdOrderByTimestampDesc(Long cronJobId, Pageable pageable);

    /**
     * Find all logs for a specific cron job by name, ordered by timestamp descending.
     *
     * @param jobName  Cron job name
     * @param pageable Pagination information
     * @return Page of cron job logs
     */
    Page<CronJobLog> findByJobNameOrderByTimestampDesc(String jobName, Pageable pageable);

    /**
     * Find logs by event type, ordered by timestamp descending.
     *
     * @param eventType Event type
     * @param pageable  Pagination information
     * @return Page of cron job logs
     */
    Page<CronJobLog> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

    /**
     * Find logs by user ID, ordered by timestamp descending.
     *
     * @param userId  User ID
     * @param pageable Pagination information
     * @return Page of cron job logs
     */
    Page<CronJobLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find logs within a time range, ordered by timestamp descending.
     *
     * @param startTime Start time
     * @param endTime   End time
     * @param pageable  Pagination information
     * @return Page of cron job logs
     */
    Page<CronJobLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * Count logs for a specific cron job.
     *
     * @param cronJobId Cron job ID
     * @return Total count of logs
     */
    long countByCronJobId(Long cronJobId);

    /**
     * Delete old logs before a certain date.
     *
     * @param beforeDate Date before which logs should be deleted
     * @return Number of deleted logs
     */
    long deleteByTimestampBefore(LocalDateTime beforeDate);
}
