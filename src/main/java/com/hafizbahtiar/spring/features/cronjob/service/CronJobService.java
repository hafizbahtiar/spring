package com.hafizbahtiar.spring.features.cronjob.service;

import com.hafizbahtiar.spring.features.cronjob.dto.CreateCronJobRequest;
import com.hafizbahtiar.spring.features.cronjob.dto.CronJobResponse;
import com.hafizbahtiar.spring.features.cronjob.dto.CronValidationResponse;
import com.hafizbahtiar.spring.features.cronjob.dto.UpdateCronJobRequest;

import java.util.List;

/**
 * Service interface for cron job management.
 * Handles CRUD operations, enable/disable, manual execution, and validation.
 */
public interface CronJobService {

    /**
     * Create a new cron job.
     *
     * @param request Create cron job request
     * @param userId  User ID who is creating the job
     * @return Created CronJobResponse
     */
    CronJobResponse createCronJob(CreateCronJobRequest request, Long userId);

    /**
     * Update an existing cron job.
     *
     * @param id      Cron job ID
     * @param request Update cron job request (partial update)
     * @param userId  User ID who is updating the job
     * @return Updated CronJobResponse
     */
    CronJobResponse updateCronJob(Long id, UpdateCronJobRequest request, Long userId);

    /**
     * Delete a cron job.
     * Also unschedules the job if it's currently scheduled.
     *
     * @param id Cron job ID
     */
    void deleteCronJob(Long id);

    /**
     * Enable a cron job (schedule it).
     *
     * @param id Cron job ID
     * @return Updated CronJobResponse
     */
    CronJobResponse enableCronJob(Long id);

    /**
     * Disable a cron job (unschedule it).
     *
     * @param id Cron job ID
     * @return Updated CronJobResponse
     */
    CronJobResponse disableCronJob(Long id);

    /**
     * Get a single cron job by ID.
     *
     * @param id Cron job ID
     * @return CronJobResponse
     */
    CronJobResponse getCronJob(Long id);

    /**
     * Get all cron jobs.
     *
     * @return List of CronJobResponse
     */
    List<CronJobResponse> getAllCronJobs();

    /**
     * Execute a cron job manually (for testing).
     *
     * @param id Cron job ID
     * @return Execution result message
     */
    String executeCronJobManually(Long id);

    /**
     * Validate a cron expression.
     *
     * @param cronExpression Cron expression to validate
     * @return CronValidationResponse with validation result and next execution times
     */
    CronValidationResponse validateCronExpression(String cronExpression);

    /**
     * Check if pg_cron extension is available for database jobs.
     *
     * @return true if pg_cron is available, false otherwise
     */
    boolean isPgCronAvailable();
}
