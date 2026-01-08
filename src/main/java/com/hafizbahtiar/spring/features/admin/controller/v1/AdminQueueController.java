package com.hafizbahtiar.spring.features.admin.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.admin.dto.CleanJobsResponse;
import com.hafizbahtiar.spring.features.admin.dto.JobHistoryResponse;
import com.hafizbahtiar.spring.features.admin.dto.QueueStatsResponse;
import com.hafizbahtiar.spring.features.admin.service.AdminQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin queue management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/queues")
@RequiredArgsConstructor
@Slf4j
public class AdminQueueController {

    private final AdminQueueService adminQueueService;

    /**
     * Get statistics for all queues.
     * Requires: OWNER/ADMIN role OR admin.queues page READ permission
     *
     * @return QueueStatsResponse with statistics for all queues
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.queues', 'READ')")
    public ResponseEntity<ApiResponse<QueueStatsResponse>> getQueueStats() {
        log.debug("GET /api/v1/admin/queues/stats - Getting queue statistics");
        QueueStatsResponse stats = adminQueueService.getQueueStats();
        return ResponseUtils.ok(stats);
    }

    /**
     * Get failed jobs for a specific queue.
     * Requires: OWNER/ADMIN role OR admin.queues page READ permission
     *
     * @param queueName Name of the queue
     * @param start     Start index (default: 0)
     * @param end       End index (default: 20)
     * @return JobHistoryResponse with failed jobs
     */
    @GetMapping("/{queueName}/jobs/failed")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.queues', 'READ')")
    public ResponseEntity<ApiResponse<JobHistoryResponse>> getFailedJobs(
            @PathVariable String queueName,
            @RequestParam(defaultValue = "0") Integer start,
            @RequestParam(defaultValue = "20") Integer end) {
        log.debug("GET /api/v1/admin/queues/{}/jobs/failed - Getting failed jobs", queueName);
        JobHistoryResponse response = adminQueueService.getFailedJobs(queueName, start, end);
        return ResponseUtils.ok(response);
    }

    /**
     * Get job history for a specific queue.
     * Requires: OWNER/ADMIN role OR admin.queues page READ permission
     *
     * @param queueName Name of the queue
     * @param status    Job status filter ("completed", "failed", "active",
     *                  "waiting", "delayed")
     * @param start     Start index (default: 0)
     * @param end       End index (default: 20)
     * @return JobHistoryResponse with job history
     */
    @GetMapping("/{queueName}/jobs/history")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.queues', 'READ')")
    public ResponseEntity<ApiResponse<JobHistoryResponse>> getJobHistory(
            @PathVariable String queueName,
            @RequestParam(defaultValue = "completed") String status,
            @RequestParam(defaultValue = "0") Integer start,
            @RequestParam(defaultValue = "20") Integer end) {
        log.debug("GET /api/v1/admin/queues/{}/jobs/history - Getting job history", queueName);
        JobHistoryResponse response = adminQueueService.getJobHistory(queueName, status, start, end);
        return ResponseUtils.ok(response);
    }

    /**
     * Retry a failed job.
     * Requires: OWNER/ADMIN role OR admin.queues page WRITE permission
     *
     * @param queueName Name of the queue
     * @param jobId     ID of the job to retry
     * @return RetryJobResponse with job ID and queue name
     */
    @PostMapping("/{queueName}/jobs/{jobId}/retry")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.queues', 'WRITE')")
    public ResponseEntity<ApiResponse<AdminQueueService.RetryJobResponse>> retryJob(
            @PathVariable String queueName,
            @PathVariable String jobId) {
        log.debug("POST /api/v1/admin/queues/{}/jobs/{}/retry - Retrying job", queueName, jobId);
        AdminQueueService.RetryJobResponse response = adminQueueService.retryJob(queueName, jobId);
        return ResponseUtils.ok(response);
    }

    /**
     * Clean jobs from a queue.
     * Requires: OWNER/ADMIN role OR admin.queues page DELETE permission
     *
     * @param queueName Name of the queue
     * @param status    Type of jobs to clean ("completed", "failed", "all")
     * @param grace     Grace period in milliseconds (default: 86400000 = 24 hours)
     * @return CleanJobsResponse with number of cleaned jobs
     */
    @DeleteMapping("/{queueName}/jobs/clean")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.queues', 'DELETE')")
    public ResponseEntity<ApiResponse<CleanJobsResponse>> cleanJobs(
            @PathVariable String queueName,
            @RequestParam(defaultValue = "completed") String status,
            @RequestParam(defaultValue = "86400000") Long grace) {
        log.debug("DELETE /api/v1/admin/queues/{}/jobs/clean - Cleaning jobs", queueName);
        CleanJobsResponse response = adminQueueService.cleanJobs(queueName, status, grace);
        return ResponseUtils.ok(response);
    }
}
