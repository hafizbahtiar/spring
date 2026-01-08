package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.model.UserActivity;
import com.hafizbahtiar.spring.features.user.repository.mongodb.UserActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for logging user activity events to MongoDB.
 * Provides methods to log various user-related events for audit and analytics
 * purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityLoggingService {

    private final UserActivityRepository userActivityRepository;

    /**
     * Log user registration event
     */
    @Async
    public void logRegistration(Long userId, String email, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("REGISTRATION")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/users", 201, null))
                    .metadata(buildMetadata("email", email))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged user registration for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log user registration event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log user profile update event
     */
    @Async
    public void logProfileUpdate(Long userId, HttpServletRequest request, Long responseTimeMs) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("PROFILE_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "PUT", "/api/v1/users/" + userId, 200, responseTimeMs))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged profile update for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log profile update event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log user profile view event
     */
    @Async
    public void logProfileView(Long userId, HttpServletRequest request, Long responseTimeMs) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("PROFILE_VIEW")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "GET", "/api/v1/users/" + userId, 200, responseTimeMs))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged profile view for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log profile view event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log email verification event
     */
    @Async
    public void logEmailVerification(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("EMAIL_VERIFICATION")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/users/" + userId + "/verify", 200, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged email verification for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log email verification event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log successful email verification
     */
    @Async
    public void logEmailVerified(Long userId, String email, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("EMAIL_VERIFIED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/auth/verify-email", 200, null))
                    .metadata(buildMetadata("email", email))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged email verified for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log email verified event", e);
        }
    }

    /**
     * Log email verification sent event
     */
    @Async
    public void logEmailVerificationSent(Long userId, String email, boolean success, String failureReason,
            HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("EMAIL_VERIFICATION_SENT")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/auth/resend-verification",
                            success ? 200 : 400, null))
                    .metadata(buildMetadata("email", email, "success", success, "failureReason", failureReason))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged email verification sent for userId: {}, success: {}", userId, success);
        } catch (Exception e) {
            log.error("Failed to log email verification sent event", e);
        }
    }

    /**
     * Log email verification failed event
     */
    @Async
    public void logEmailVerificationFailed(Long userId, String email, String failureReason,
            HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("EMAIL_VERIFICATION_FAILED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/auth/verify-email", 400, null))
                    .metadata(buildMetadata("email", email, "failureReason", failureReason))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged email verification failed for userId: {}, reason: {}", userId, failureReason);
        } catch (Exception e) {
            log.error("Failed to log email verification failed event", e);
        }
    }

    /**
     * Build metadata object with multiple key-value pairs
     */
    private Object buildMetadata(Object... keyValues) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length) {
                metadata.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
            }
        }
        return metadata;
    }

    /**
     * Log user deactivation event
     */
    @Async
    public void logDeactivation(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("DEACTIVATION")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "DELETE", "/api/v1/users/" + userId, 204, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged user deactivation for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log deactivation event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log user preferences update event
     */
    @Async
    public void logPreferencesUpdated(Long preferencesId, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("PREFERENCES_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "PUT", "/api/v1/settings/preferences", 200, responseTimeMs))
                    .metadata(buildMetadata("preferencesId", preferencesId))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged preferences update for userId: {}, preferencesId: {}", userId, preferencesId);
        } catch (Exception e) {
            log.error("Failed to log preferences update event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log notification preferences update event
     */
    @Async
    public void logNotificationPreferencesUpdated(Long preferencesId, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("NOTIFICATION_PREFERENCES_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "PUT", "/api/v1/notifications/preferences", 200,
                            responseTimeMs))
                    .metadata(buildMetadata("preferencesId", preferencesId))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged notification preferences update for userId: {}, preferencesId: {}", userId,
                    preferencesId);
        } catch (Exception e) {
            log.error("Failed to log notification preferences update event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log currency preferences update event
     */
    @Async
    public void logCurrencyPreferencesUpdated(Long preferencesId, Long userId, HttpServletRequest request,
            Long responseTimeMs) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("CURRENCY_PREFERENCES_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "PUT", "/api/v1/settings/currency/preferences", 200,
                            responseTimeMs))
                    .metadata(buildMetadata("preferencesId", preferencesId))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged currency preferences update for userId: {}, preferencesId: {}", userId,
                    preferencesId);
        } catch (Exception e) {
            log.error("Failed to log currency preferences update event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Log generic API call event
     */
    @Async
    public void logApiCall(Long userId, String endpoint, String method, Integer responseStatus,
            Long responseTimeMs, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("API_CALL")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, method, endpoint, responseStatus, responseTimeMs))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged API call for userId: {}, endpoint: {}", userId, endpoint);
        } catch (Exception e) {
            log.error("Failed to log API call event", e);
            // Don't throw exception - logging failure shouldn't break user operations
        }
    }

    /**
     * Build activity details from request
     */
    private UserActivity.ActivityDetails buildActivityDetails(HttpServletRequest request, String method,
            String endpoint, Integer responseStatus,
            Long responseTimeMs) {
        return UserActivity.ActivityDetails.builder()
                .endpoint(endpoint)
                .method(method)
                .responseStatus(responseStatus)
                .responseTimeMs(responseTimeMs)
                .userAgent(getUserAgent(request))
                .ipAddress(getClientIpAddress(request))
                .requestId(getRequestId(request))
                .build();
    }

    /**
     * Build metadata object
     */
    private Object buildMetadata(String key, Object value) {
        return java.util.Map.of(key, value);
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
     * Log password change event
     */
    @Async
    public void logPasswordChanged(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("PASSWORD_CHANGED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/auth/change-password", 200, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged password change for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log password change event", e);
        }
    }

    /**
     * Log password change failed event
     */
    @Async
    public void logPasswordChangeFailed(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("PASSWORD_CHANGE_FAILED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/auth/change-password", 400, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged password change failed for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log password change failed event", e);
        }
    }

    /**
     * Log account deletion requested event
     */
    @Async
    public void logAccountDeletionRequested(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("ACCOUNT_DELETION_REQUESTED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/users/account/request-deletion", 200, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged account deletion requested for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log account deletion requested event", e);
        }
    }

    /**
     * Log account deleted event
     */
    @Async
    public void logAccountDeleted(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("ACCOUNT_DELETED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "DELETE", "/api/v1/users/account", 200, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged account deleted for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log account deleted event", e);
        }
    }

    /**
     * Log account deactivated event
     */
    @Async
    public void logAccountDeactivated(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("ACCOUNT_DEACTIVATED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/users/account/deactivate", 200, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged account deactivated for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log account deactivated event", e);
        }
    }

    /**
     * Log account reactivated event
     */
    @Async
    public void logAccountReactivated(Long userId, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("ACCOUNT_REACTIVATED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "POST", "/api/v1/users/account/reactivate", 200, null))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged account reactivated for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log account reactivated event", e);
        }
    }

    /**
     * Log data exported event
     */
    @Async
    public void logDataExported(Long userId, String format, HttpServletRequest request) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType("DATA_EXPORTED")
                    .timestamp(LocalDateTime.now())
                    .sessionId(getSessionId(request))
                    .details(buildActivityDetails(request, "GET", "/api/v1/users/account/export?format=" + format, 200, null))
                    .metadata(buildMetadata("format", format))
                    .build();

            userActivityRepository.save(activity);
            log.debug("Logged data exported for userId: {}, format: {}", userId, format);
        } catch (Exception e) {
            log.error("Failed to log data exported event", e);
        }
    }

    /**
     * Get request ID from request (if available)
     */
    private String getRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // Try to get request ID from header or attribute
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = (String) request.getAttribute("requestId");
        }
        return requestId;
    }
}
