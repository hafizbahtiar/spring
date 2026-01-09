package com.hafizbahtiar.spring.features.cronjob.service;

import com.hafizbahtiar.spring.features.cronjob.model.CronJobLog;
import com.hafizbahtiar.spring.features.cronjob.repository.mongodb.CronJobLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for logging cron job events to MongoDB.
 * Provides methods to log various cron job-related events for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CronJobLoggingService {

    private final CronJobLogRepository cronJobLogRepository;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CronJobLoggingService.class);

    /**
     * Log cron job creation event
     */
    @Async
    public void logJobCreated(Long cronJobId, String jobName, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            CronJobLog jobLog = buildLog(cronJobId, jobName, "JOB_CREATED", userId, request, true, null, responseTimeMs,
                    null);
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job creation for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job creation event", e);
        }
    }

    /**
     * Log cron job update event
     */
    @Async
    public void logJobUpdated(Long cronJobId, String jobName, Long userId, HttpServletRequest request,
            Long responseTimeMs, Map<String, Object> changes) {
        try {
            CronJobLog jobLog = buildLog(cronJobId, jobName, "JOB_UPDATED", userId, request, true, null, responseTimeMs,
                    changes);
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job update for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job update event", e);
        }
    }

    /**
     * Log cron job deletion event
     */
    @Async
    public void logJobDeleted(Long cronJobId, String jobName, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            CronJobLog jobLog = buildLog(cronJobId, jobName, "JOB_DELETED", userId, request, true, null, responseTimeMs,
                    null);
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job deletion for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job deletion event", e);
        }
    }

    /**
     * Log cron job enabled event
     */
    @Async
    public void logJobEnabled(Long cronJobId, String jobName, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            CronJobLog jobLog = buildLog(cronJobId, jobName, "JOB_ENABLED", userId, request, true, null, responseTimeMs,
                    null);
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job enabled for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job enabled event", e);
        }
    }

    /**
     * Log cron job disabled event
     */
    @Async
    public void logJobDisabled(Long cronJobId, String jobName, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            CronJobLog jobLog = buildLog(cronJobId, jobName, "JOB_DISABLED", userId, request, true, null, responseTimeMs,
                    null);
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job disabled for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job disabled event", e);
        }
    }

    /**
     * Log manual cron job execution event
     */
    @Async
    public void logManualExecution(Long cronJobId, String jobName, Long userId, HttpServletRequest request,
            boolean success, String error, Long responseTimeMs) {
        try {
            CronJobLog jobLog = buildLog(cronJobId, jobName, "JOB_MANUAL_EXECUTION", userId, request, success, error,
                    responseTimeMs, null);
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged manual cron job execution for jobId: {}, jobName: {}, success: {}", cronJobId, jobName,
                    success);
        } catch (Exception e) {
            logger.error("Failed to log manual cron job execution event", e);
        }
    }

    /**
     * Log cron job scheduled event (when job is scheduled in scheduler)
     */
    @Async
    public void logJobScheduled(Long cronJobId, String jobName) {
        try {
            CronJobLog jobLog = CronJobLog.builder()
                    .cronJobId(cronJobId)
                    .jobName(jobName)
                    .eventType("JOB_SCHEDULED")
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job scheduled for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job scheduled event", e);
        }
    }

    /**
     * Log cron job unscheduled event (when job is removed from scheduler)
     */
    @Async
    public void logJobUnscheduled(Long cronJobId, String jobName) {
        try {
            CronJobLog jobLog = CronJobLog.builder()
                    .cronJobId(cronJobId)
                    .jobName(jobName)
                    .eventType("JOB_UNSCHEDULED")
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();
            cronJobLogRepository.save(jobLog);
            logger.debug("Logged cron job unscheduled for jobId: {}, jobName: {}", cronJobId, jobName);
        } catch (Exception e) {
            logger.error("Failed to log cron job unscheduled event", e);
        }
    }

    /**
     * Build a CronJobLog from parameters
     */
    private CronJobLog buildLog(Long cronJobId, String jobName, String eventType, Long userId,
            HttpServletRequest request, boolean success, String failureReason, Long responseTimeMs,
            Map<String, Object> metadata) {
        return CronJobLog.builder()
                .cronJobId(cronJobId)
                .jobName(jobName)
                .eventType(eventType)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .ipAddress(getClientIpAddress(request))
                .userAgent(getUserAgent(request))
                .sessionId(getSessionId(request))
                .requestId(getRequestId(request))
                .success(success)
                .failureReason(failureReason)
                .responseTimeMs(responseTimeMs)
                .metadata(metadata)
                .build();
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : "unknown";
    }

    /**
     * Get session ID from request
     */
    private String getSessionId(HttpServletRequest request) {
        return request != null && request.getSession(false) != null
                ? request.getSession(false).getId()
                : null;
    }

    /**
     * Get request ID from request (if available)
     */
    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = (String) request.getAttribute("requestId");
        }
        return requestId;
    }
}
