package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.CleanJobsResponse;
import com.hafizbahtiar.spring.features.admin.dto.JobHistoryResponse;
import com.hafizbahtiar.spring.features.admin.dto.QueueStatsResponse;

/**
 * Service interface for admin queue management.
 */
public interface AdminQueueService {

    /**
     * Get statistics for all queues.
     *
     * @return QueueStatsResponse with statistics for all queues
     */
    QueueStatsResponse getQueueStats();

    /**
     * Get failed jobs for a specific queue.
     *
     * @param queueName Name of the queue
     * @param start     Start index (for pagination)
     * @param end       End index (for pagination)
     * @return List of failed jobs and total count
     */
    JobHistoryResponse getFailedJobs(String queueName, Integer start, Integer end);

    /**
     * Get job history for a specific queue.
     *
     * @param queueName Name of the queue
     * @param status    Job status filter ("completed", "failed", "active",
     *                  "waiting", "delayed")
     * @param start     Start index (for pagination)
     * @param end       End index (for pagination)
     * @return JobHistoryResponse with jobs and total count
     */
    JobHistoryResponse getJobHistory(String queueName, String status, Integer start, Integer end);

    /**
     * Retry a failed job.
     *
     * @param queueName Name of the queue
     * @param jobId     ID of the job to retry
     * @return Job ID and queue name
     */
    RetryJobResponse retryJob(String queueName, String jobId);

    /**
     * Clean jobs from a queue.
     *
     * @param queueName Name of the queue
     * @param status    Type of jobs to clean ("completed", "failed", "all")
     * @param grace     Grace period in milliseconds
     * @return CleanJobsResponse with number of cleaned jobs
     */
    CleanJobsResponse cleanJobs(String queueName, String status, Long grace);

    /**
     * Response for retry job operation.
     */
    record RetryJobResponse(String jobId, String queueName) {
    }
}
