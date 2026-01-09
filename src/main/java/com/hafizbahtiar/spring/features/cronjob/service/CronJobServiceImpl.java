package com.hafizbahtiar.spring.features.cronjob.service;

import com.hafizbahtiar.spring.features.admin.model.CronJobExecutionLog;
import com.hafizbahtiar.spring.features.admin.repository.mongodb.CronJobExecutionLogRepository;
import com.hafizbahtiar.spring.features.cronjob.dto.CreateCronJobRequest;
import com.hafizbahtiar.spring.features.cronjob.dto.CronJobResponse;
import com.hafizbahtiar.spring.features.cronjob.dto.CronValidationResponse;
import com.hafizbahtiar.spring.features.cronjob.dto.UpdateCronJobRequest;
import com.hafizbahtiar.spring.features.cronjob.entity.CronJob;
import com.hafizbahtiar.spring.features.cronjob.entity.JobType;
import com.hafizbahtiar.spring.features.cronjob.exception.CronJobException;
import com.hafizbahtiar.spring.features.cronjob.exception.CronJobNotFoundException;
import com.hafizbahtiar.spring.features.cronjob.executor.ApplicationJobExecutor;
import com.hafizbahtiar.spring.features.cronjob.repository.CronJobRepository;
import com.hafizbahtiar.spring.features.cronjob.scheduler.DatabaseCronJobScheduler;
import com.hafizbahtiar.spring.features.cronjob.scheduler.DynamicCronJobScheduler;
import com.hafizbahtiar.spring.features.cronjob.scheduler.JobExecutionWrapper;
import com.hafizbahtiar.spring.features.cronjob.service.SqlScriptValidator.ValidationResult;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CronJobService.
 * Handles CRUD operations, scheduling, validation, and execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CronJobServiceImpl implements CronJobService {

    private final CronJobRepository cronJobRepository;
    private final UserRepository userRepository;
    private final DynamicCronJobScheduler dynamicCronJobScheduler;
    private final DatabaseCronJobScheduler databaseCronJobScheduler;
    private final ApplicationJobExecutor applicationJobExecutor;
    private final JobExecutionWrapper jobExecutionWrapper;
    private final CronJobLoggingService cronJobLoggingService;
    private final CronJobExecutionLogRepository cronJobExecutionLogRepository;
    private final SqlScriptValidator sqlScriptValidator;

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not retrieve current request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public CronJobResponse createCronJob(CreateCronJobRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Creating cron job: {} for user ID: {}", request.getName(), userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Check if job name already exists
        if (cronJobRepository.existsByNameIgnoreCase(request.getName())) {
            throw CronJobException.nameAlreadyExists(request.getName());
        }

        // Validate cron expression
        validateCronExpressionInternal(request.getCronExpression());

        // Validate job configuration based on type
        if (request.getJobType() == JobType.APPLICATION) {
            if (request.getJobClass() == null || request.getJobClass().trim().isEmpty()) {
                throw CronJobException.invalidConfiguration("Job class is required for APPLICATION type jobs");
            }
        } else if (request.getJobType() == JobType.DATABASE) {
            if (request.getSqlScript() == null || request.getSqlScript().trim().isEmpty()) {
                throw CronJobException.invalidConfiguration("SQL script is required for DATABASE type jobs");
            }
            // Validate SQL script syntax and safety
            ValidationResult sqlValidation = sqlScriptValidator.validate(request.getSqlScript());
            if (!sqlValidation.isValid()) {
                throw CronJobException.invalidSqlScript(sqlValidation.getError());
            }
        }

        // Create entity
        CronJob cronJob = new CronJob();
        cronJob.setName(request.getName());
        cronJob.setDescription(request.getDescription());
        cronJob.setJobType(request.getJobType());
        cronJob.setCronExpression(request.getCronExpression());
        cronJob.setJobClass(request.getJobClass());
        cronJob.setSqlScript(request.getSqlScript());
        cronJob.setCreatedBy(user);
        cronJob.setEnabled(request.getEnabled() != null ? request.getEnabled() : false);

        // Validate job class/method if APPLICATION type
        if (request.getJobType() == JobType.APPLICATION) {
            CronJob tempJob = new CronJob();
            tempJob.setJobClass(request.getJobClass());
            tempJob.setName(request.getName());
            if (!applicationJobExecutor.validate(tempJob)) {
                throw CronJobException.jobClassNotFound(request.getJobClass());
            }
        }

        // Save entity
        CronJob savedJob = cronJobRepository.save(cronJob);
        log.info("Created cron job ID: {}, name: {}", savedJob.getId(), savedJob.getName());

        // Schedule job if enabled
        if (savedJob.isEnabled()) {
            try {
                scheduleJob(savedJob);
                cronJobLoggingService.logJobScheduled(savedJob.getId(), savedJob.getName());
            } catch (Exception e) {
                log.error("Failed to schedule job after creation: {}", savedJob.getName(), e);
                // Don't fail creation, just log the error
            }
        }

        // Log to MongoDB
        long responseTime = System.currentTimeMillis() - startTime;
        cronJobLoggingService.logJobCreated(savedJob.getId(), savedJob.getName(), userId, getCurrentRequest(),
                responseTime);

        return toResponse(savedJob);
    }

    @Override
    public CronJobResponse updateCronJob(Long id, UpdateCronJobRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Updating cron job ID: {} for user ID: {}", id, userId);

        CronJob cronJob = cronJobRepository.findById(id)
                .orElseThrow(() -> CronJobNotFoundException.byId(id));

        boolean wasEnabled = cronJob.isEnabled();
        boolean needsReschedule = false;
        java.util.Map<String, Object> changes = new java.util.HashMap<>();

        // Update name if provided
        if (request.getName() != null && !request.getName().equals(cronJob.getName())) {
            if (cronJobRepository.existsByNameIgnoreCase(request.getName())) {
                throw CronJobException.nameAlreadyExists(request.getName());
            }
            changes.put("name", request.getName());
            cronJob.setName(request.getName());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            changes.put("description", request.getDescription());
            cronJob.setDescription(request.getDescription());
        }

        // Update cron expression if provided
        if (request.getCronExpression() != null) {
            validateCronExpressionInternal(request.getCronExpression());
            changes.put("cronExpression", request.getCronExpression());
            cronJob.setCronExpression(request.getCronExpression());
            needsReschedule = wasEnabled;
        }

        // Update job class if provided (APPLICATION only)
        if (request.getJobClass() != null && cronJob.getJobType() == JobType.APPLICATION) {
            CronJob tempJob = new CronJob();
            tempJob.setJobClass(request.getJobClass());
            tempJob.setName(cronJob.getName());
            if (!applicationJobExecutor.validate(tempJob)) {
                throw CronJobException.jobClassNotFound(request.getJobClass());
            }
            changes.put("jobClass", request.getJobClass());
            cronJob.setJobClass(request.getJobClass());
            needsReschedule = wasEnabled;
        }

        // Update SQL script if provided (DATABASE only)
        if (request.getSqlScript() != null && cronJob.getJobType() == JobType.DATABASE) {
            // Validate SQL script syntax and safety
            ValidationResult sqlValidation = sqlScriptValidator.validate(request.getSqlScript());
            if (!sqlValidation.isValid()) {
                throw CronJobException.invalidSqlScript(sqlValidation.getError());
            }
            changes.put("sqlScript", request.getSqlScript());
            cronJob.setSqlScript(request.getSqlScript());
            needsReschedule = wasEnabled;
        }

        // Update enabled status
        if (request.getEnabled() != null) {
            boolean newEnabled = request.getEnabled();
            if (newEnabled != wasEnabled) {
                changes.put("enabled", newEnabled);
                if (newEnabled) {
                    cronJob.enable();
                } else {
                    cronJob.disable();
                }
                needsReschedule = true;
            }
        }

        // Save entity
        CronJob updatedJob = cronJobRepository.save(cronJob);

        // Reschedule if needed
        if (needsReschedule) {
            try {
                if (updatedJob.isEnabled()) {
                    scheduleJob(updatedJob);
                    cronJobLoggingService.logJobScheduled(updatedJob.getId(), updatedJob.getName());
                } else {
                    unscheduleJob(updatedJob);
                    cronJobLoggingService.logJobUnscheduled(updatedJob.getId(), updatedJob.getName());
                }
            } catch (Exception e) {
                log.error("Failed to reschedule job after update: {}", updatedJob.getName(), e);
            }
        }

        // Log to MongoDB
        long responseTime = System.currentTimeMillis() - startTime;
        cronJobLoggingService.logJobUpdated(updatedJob.getId(), updatedJob.getName(), userId, getCurrentRequest(),
                responseTime, changes);

        return toResponse(updatedJob);
    }

    @Override
    public void deleteCronJob(Long id) {
        long startTime = System.currentTimeMillis();
        log.info("Deleting cron job ID: {}", id);

        CronJob cronJob = cronJobRepository.findById(id)
                .orElseThrow(() -> CronJobNotFoundException.byId(id));

        String jobName = cronJob.getName();
        Long jobId = cronJob.getId();

        // Unschedule if enabled
        if (cronJob.isEnabled()) {
            try {
                unscheduleJob(cronJob);
                cronJobLoggingService.logJobUnscheduled(jobId, jobName);
            } catch (Exception e) {
                log.error("Failed to unschedule job before deletion: {}", jobName, e);
            }
        }

        // Delete entity
        cronJobRepository.delete(cronJob);
        log.info("Deleted cron job ID: {}, name: {}", jobId, jobName);

        // Log to MongoDB
        long responseTime = System.currentTimeMillis() - startTime;
        cronJobLoggingService.logJobDeleted(jobId, jobName, null, getCurrentRequest(), responseTime);
    }

    @Override
    public CronJobResponse enableCronJob(Long id) {
        long startTime = System.currentTimeMillis();
        log.info("Enabling cron job ID: {}", id);

        CronJob cronJob = cronJobRepository.findById(id)
                .orElseThrow(() -> CronJobNotFoundException.byId(id));

        if (cronJob.isEnabled()) {
            log.debug("Job {} is already enabled", cronJob.getName());
            return toResponse(cronJob);
        }

        cronJob.enable();
        CronJob savedJob = cronJobRepository.save(cronJob);

        // Schedule the job
        try {
            scheduleJob(savedJob);
            cronJobLoggingService.logJobScheduled(savedJob.getId(), savedJob.getName());
        } catch (Exception e) {
            log.error("Failed to schedule job: {}", savedJob.getName(), e);
            throw CronJobException.invalidConfiguration("Failed to schedule job: " + e.getMessage());
        }

        // Log to MongoDB
        long responseTime = System.currentTimeMillis() - startTime;
        cronJobLoggingService.logJobEnabled(savedJob.getId(), savedJob.getName(), null, getCurrentRequest(),
                responseTime);

        return toResponse(savedJob);
    }

    @Override
    public CronJobResponse disableCronJob(Long id) {
        long startTime = System.currentTimeMillis();
        log.info("Disabling cron job ID: {}", id);

        CronJob cronJob = cronJobRepository.findById(id)
                .orElseThrow(() -> CronJobNotFoundException.byId(id));

        if (!cronJob.isEnabled()) {
            log.debug("Job {} is already disabled", cronJob.getName());
            return toResponse(cronJob);
        }

        cronJob.disable();
        CronJob savedJob = cronJobRepository.save(cronJob);

        // Unschedule the job
        try {
            unscheduleJob(savedJob);
            cronJobLoggingService.logJobUnscheduled(savedJob.getId(), savedJob.getName());
        } catch (Exception e) {
            log.error("Failed to unschedule job: {}", savedJob.getName(), e);
        }

        // Log to MongoDB
        long responseTime = System.currentTimeMillis() - startTime;
        cronJobLoggingService.logJobDisabled(savedJob.getId(), savedJob.getName(), null, getCurrentRequest(),
                responseTime);

        return toResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public CronJobResponse getCronJob(Long id) {
        log.debug("Getting cron job ID: {}", id);

        CronJob cronJob = cronJobRepository.findById(id)
                .orElseThrow(() -> CronJobNotFoundException.byId(id));

        return toResponse(cronJob);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CronJobResponse> getAllCronJobs() {
        log.debug("Getting all cron jobs");

        List<CronJob> cronJobs = cronJobRepository.findAll();
        return cronJobs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String executeCronJobManually(Long id) {
        long startTime = System.currentTimeMillis();
        log.info("Manually executing cron job ID: {}", id);

        CronJob cronJob = cronJobRepository.findById(id)
                .orElseThrow(() -> CronJobNotFoundException.byId(id));

        if (!cronJob.isValid()) {
            throw CronJobException.invalidConfiguration("Job configuration is invalid");
        }

        boolean success = false;
        String error = null;

        try {
            if (cronJob.getJobType() == JobType.APPLICATION) {
                // Use JobExecutionWrapper for consistent logging to MongoDB execution logs
                jobExecutionWrapper.executeWithLogging(cronJob, () -> {
                    try {
                        applicationJobExecutor.execute(cronJob);
                    } catch (ApplicationJobExecutor.JobExecutionException e) {
                        throw new RuntimeException("Job execution failed: " + e.getMessage(), e);
                    }
                });
                success = true;
            } else if (cronJob.getJobType() == JobType.DATABASE) {
                // For database jobs, we can't execute them manually from the application
                // They need to be executed via pg_cron or manually in the database
                throw CronJobException.invalidConfiguration(
                        "DATABASE type jobs cannot be executed manually from the application");
            }
        } catch (Exception e) {
            error = e.getMessage();
            log.error("Manual execution failed for job: {}", cronJob.getName(), e);
        }

        // Log to MongoDB audit log (separate from execution log)
        long responseTime = System.currentTimeMillis() - startTime;
        cronJobLoggingService.logManualExecution(cronJob.getId(), cronJob.getName(), null, getCurrentRequest(),
                success, error, responseTime);

        if (success) {
            return "Job executed successfully";
        } else {
            throw new RuntimeException("Job execution failed: " + (error != null ? error : "Unknown error"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CronValidationResponse validateCronExpression(String cronExpression) {
        log.debug("Validating cron expression: {}", cronExpression);

        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            List<LocalDateTime> nextExecutions = new ArrayList<>();

            // Calculate next 5 execution times
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < 5; i++) {
                LocalDateTime next = cron.next(now);
                if (next != null) {
                    nextExecutions.add(next);
                    now = next;
                } else {
                    break;
                }
            }

            return CronValidationResponse.builder()
                    .valid(true)
                    .error(null)
                    .nextExecutions(nextExecutions)
                    .build();
        } catch (Exception e) {
            return CronValidationResponse.builder()
                    .valid(false)
                    .error(e.getMessage())
                    .nextExecutions(new ArrayList<>())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPgCronAvailable() {
        return databaseCronJobScheduler.isPgCronAvailable();
    }

    /**
     * Schedule a job based on its type
     */
    private void scheduleJob(CronJob cronJob) {
        if (cronJob.getJobType() == JobType.APPLICATION) {
            dynamicCronJobScheduler.scheduleJob(cronJob);
        } else if (cronJob.getJobType() == JobType.DATABASE) {
            if (!databaseCronJobScheduler.isPgCronAvailable()) {
                throw CronJobException.pgCronNotAvailable();
            }
            databaseCronJobScheduler.scheduleDatabaseJob(cronJob);
        }
    }

    /**
     * Unschedule a job based on its type
     */
    private void unscheduleJob(CronJob cronJob) {
        if (cronJob.getJobType() == JobType.APPLICATION) {
            dynamicCronJobScheduler.unscheduleJob(cronJob.getName());
        } else if (cronJob.getJobType() == JobType.DATABASE) {
            databaseCronJobScheduler.unscheduleDatabaseJob(cronJob.getName());
        }
    }

    /**
     * Validate cron expression internally
     */
    private void validateCronExpressionInternal(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
        } catch (Exception e) {
            throw CronJobException.invalidCronExpression(cronExpression, e.getMessage());
        }
    }

    /**
     * Convert CronJob entity to CronJobResponse DTO
     */
    private CronJobResponse toResponse(CronJob cronJob) {
        // Get execution statistics from MongoDB
        long executionCount = cronJobExecutionLogRepository.countByJobName(cronJob.getName());
        long successCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(cronJob.getName(), true);
        long failureCount = cronJobExecutionLogRepository.countByJobNameAndSuccess(cronJob.getName(), false);

        // Get latest execution
        CronJobExecutionLog latestExecution = cronJobExecutionLogRepository
                .findFirstByJobNameOrderByExecutedAtDesc(cronJob.getName());

        // Calculate next execution time
        LocalDateTime nextExecution = null;
        try {
            CronExpression cron = CronExpression.parse(cronJob.getCronExpression());
            nextExecution = cron.next(LocalDateTime.now());
        } catch (Exception e) {
            log.debug("Could not calculate next execution time for job: {}", cronJob.getName(), e);
        }

        CronJobResponse.CronJobResponseBuilder builder = CronJobResponse.builder()
                .id(cronJob.getId())
                .name(cronJob.getName())
                .description(cronJob.getDescription())
                .jobType(cronJob.getJobType())
                .cronExpression(cronJob.getCronExpression())
                .enabled(cronJob.isEnabled())
                .jobClass(cronJob.getJobClass())
                .sqlScript(cronJob.getSqlScript())
                .createdBy(cronJob.getCreatedBy().getId())
                .createdByName(cronJob.getCreatedBy().getFullName())
                .createdAt(cronJob.getCreatedAt())
                .updatedAt(cronJob.getUpdatedAt())
                .executionCount(executionCount)
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
}
