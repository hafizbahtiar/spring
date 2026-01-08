package com.hafizbahtiar.spring.features.admin.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.admin.dto.CronJobHistoryResponse;
import com.hafizbahtiar.spring.features.admin.dto.CronJobStatus;
import com.hafizbahtiar.spring.features.admin.service.AdminCronJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin cron job monitoring endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/cron-jobs")
@RequiredArgsConstructor
@Slf4j
public class AdminCronJobController {

    private final AdminCronJobService adminCronJobService;

    /**
     * Get status for all cron jobs.
     * Requires: OWNER/ADMIN role OR admin.cron-jobs page READ permission
     *
     * @return List of cron job statuses
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.cron-jobs', 'READ')")
    public ResponseEntity<ApiResponse<CronJobsResponse>> getAllCronJobStatuses() {
        log.debug("GET /api/v1/admin/cron-jobs - Getting all cron job statuses");
        List<CronJobStatus> jobs = adminCronJobService.getAllCronJobStatuses();
        return ResponseUtils.ok(new CronJobsResponse(jobs));
    }

    /**
     * Get status for a specific cron job.
     * Requires: OWNER/ADMIN role OR admin.cron-jobs page READ permission
     *
     * @param jobName Name of the cron job
     * @return CronJobStatus for the specified job
     */
    @GetMapping("/{jobName}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.cron-jobs', 'READ')")
    public ResponseEntity<ApiResponse<CronJobStatus>> getCronJobStatus(@PathVariable String jobName) {
        log.debug("GET /api/v1/admin/cron-jobs/{} - Getting cron job status", jobName);
        CronJobStatus status = adminCronJobService.getCronJobStatus(jobName);
        return ResponseUtils.ok(status);
    }

    /**
     * Get execution history for a specific cron job.
     * Requires: OWNER/ADMIN role OR admin.cron-jobs page READ permission
     *
     * @param jobName Name of the cron job
     * @param limit   Maximum number of executions to return (default: 50)
     * @return CronJobHistoryResponse with execution history
     */
    @GetMapping("/{jobName}/history")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.cron-jobs', 'READ')")
    public ResponseEntity<ApiResponse<CronJobHistoryResponse>> getCronJobHistory(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "50") Integer limit) {
        log.debug("GET /api/v1/admin/cron-jobs/{}/history - Getting cron job history", jobName);
        CronJobHistoryResponse history = adminCronJobService.getCronJobHistory(jobName, limit);
        return ResponseUtils.ok(history);
    }

    /**
     * Response wrapper for list of cron jobs.
     */
    public record CronJobsResponse(List<CronJobStatus> jobs) {
    }
}
