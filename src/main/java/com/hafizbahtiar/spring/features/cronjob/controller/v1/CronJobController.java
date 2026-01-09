package com.hafizbahtiar.spring.features.cronjob.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.cronjob.dto.CreateCronJobRequest;
import com.hafizbahtiar.spring.features.cronjob.dto.CronJobResponse;
import com.hafizbahtiar.spring.features.cronjob.dto.CronValidationResponse;
import com.hafizbahtiar.spring.features.cronjob.dto.UpdateCronJobRequest;
import com.hafizbahtiar.spring.features.cronjob.registry.JobRegistry;
import com.hafizbahtiar.spring.features.cronjob.service.CronJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for cron job management endpoints.
 * Handles CRUD operations, enable/disable, manual execution, and validation.
 */
@RestController
@RequestMapping("/api/v1/cron-jobs")
@RequiredArgsConstructor
@Slf4j
public class CronJobController {

    private final CronJobService cronJobService;
    private final JobRegistry jobRegistry;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Create a new cron job
     * POST /api/v1/cron-jobs
     * Requires: OWNER role
     */
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CronJobResponse>> createCronJob(
            @Valid @RequestBody CreateCronJobRequest request) {
        Long userId = getCurrentUserId();
        log.info("Cron job creation request received: {} for user ID: {}", request.getName(), userId);
        CronJobResponse response = cronJobService.createCronJob(request, userId);
        return ResponseUtils.created(response, "Cron job created successfully");
    }

    /**
     * Update an existing cron job
     * PUT /api/v1/cron-jobs/{id}
     * Requires: OWNER role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CronJobResponse>> updateCronJob(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCronJobRequest request) {
        Long userId = getCurrentUserId();
        log.info("Cron job update request received for ID: {} by user ID: {}", id, userId);
        CronJobResponse response = cronJobService.updateCronJob(id, request, userId);
        return ResponseUtils.ok(response, "Cron job updated successfully");
    }

    /**
     * Delete a cron job
     * DELETE /api/v1/cron-jobs/{id}
     * Requires: OWNER role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteCronJob(@PathVariable Long id) {
        log.info("Cron job deletion request received for ID: {}", id);
        cronJobService.deleteCronJob(id);
        return ResponseUtils.noContent();
    }

    /**
     * Get all cron jobs
     * GET /api/v1/cron-jobs
     * Requires: OWNER or ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CronJobResponse>>> getAllCronJobs() {
        log.debug("Fetching all cron jobs");
        List<CronJobResponse> jobs = cronJobService.getAllCronJobs();
        return ResponseUtils.ok(jobs);
    }

    /**
     * Get a single cron job by ID
     * GET /api/v1/cron-jobs/{id}
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CronJobResponse>> getCronJob(@PathVariable Long id) {
        log.debug("Fetching cron job ID: {}", id);
        CronJobResponse response = cronJobService.getCronJob(id);
        return ResponseUtils.ok(response);
    }

    /**
     * Enable a cron job
     * POST /api/v1/cron-jobs/{id}/enable
     * Requires: OWNER role
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CronJobResponse>> enableCronJob(@PathVariable Long id) {
        log.info("Cron job enable request received for ID: {}", id);
        CronJobResponse response = cronJobService.enableCronJob(id);
        return ResponseUtils.ok(response, "Cron job enabled successfully");
    }

    /**
     * Disable a cron job
     * POST /api/v1/cron-jobs/{id}/disable
     * Requires: OWNER role
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CronJobResponse>> disableCronJob(@PathVariable Long id) {
        log.info("Cron job disable request received for ID: {}", id);
        CronJobResponse response = cronJobService.disableCronJob(id);
        return ResponseUtils.ok(response, "Cron job disabled successfully");
    }

    /**
     * Execute a cron job manually
     * POST /api/v1/cron-jobs/{id}/execute
     * Requires: OWNER role
     */
    @PostMapping("/{id}/execute")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<String>> executeCronJob(@PathVariable Long id) {
        log.info("Manual cron job execution request received for ID: {}", id);
        String result = cronJobService.executeCronJobManually(id);
        return ResponseUtils.ok(result, "Cron job executed successfully");
    }

    /**
     * Validate a cron expression
     * GET /api/v1/cron-jobs/validate-cron?expression={expr}
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/validate-cron")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CronValidationResponse>> validateCronExpression(
            @RequestParam("expression") String cronExpression) {
        log.debug("Cron expression validation request: {}", cronExpression);
        CronValidationResponse response = cronJobService.validateCronExpression(cronExpression);
        return ResponseUtils.ok(response);
    }

    /**
     * Get available predefined jobs
     * GET /api/v1/cron-jobs/available-jobs
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/available-jobs")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<JobRegistry.JobDefinition>>> getAvailableJobs(
            @RequestParam(required = false) String serviceName) {
        log.debug("Fetching available jobs, serviceName: {}", serviceName);
        List<JobRegistry.JobDefinition> jobs;
        if (serviceName != null && !serviceName.trim().isEmpty()) {
            jobs = jobRegistry.getJobsByService(serviceName.trim());
        } else {
            jobs = jobRegistry.getAvailableJobs();
        }
        return ResponseUtils.ok(jobs);
    }

    /**
     * Check if pg_cron extension is available for database jobs
     * GET /api/v1/cron-jobs/pg-cron-available
     * Requires: OWNER or ADMIN role
     */
    @GetMapping("/pg-cron-available")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkPgCronAvailable() {
        log.debug("Checking pg_cron availability");
        boolean available = cronJobService.isPgCronAvailable();
        return ResponseUtils.ok(available);
    }
}
