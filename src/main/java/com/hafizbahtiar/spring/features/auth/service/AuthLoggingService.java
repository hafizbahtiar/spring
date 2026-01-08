package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.model.AuthLog;
import com.hafizbahtiar.spring.features.auth.repository.mongodb.AuthLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for logging authentication events to MongoDB.
 * Provides methods to log various authentication-related events for audit and
 * security purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthLoggingService {

    private final AuthLogRepository authLogRepository;

    /**
     * Log a successful login event
     */
    @Async
    public void logLoginSuccess(Long userId, String identifier, String sessionId,
            LocalDateTime tokenExpiresAt, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("LOGIN_SUCCESS")
                    .userId(userId)
                    .identifier(identifier)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(sessionId)
                    .requestId(getRequestId(request))
                    .success(true)
                    .tokenExpiresAt(tokenExpiresAt)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged successful login for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log login success event", e);
            // Don't throw exception - logging failure shouldn't break authentication flow
        }
    }

    /**
     * Log a failed login attempt
     */
    @Async
    public void logLoginFailure(String identifier, String failureReason, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("LOGIN_FAILURE")
                    .userId(null) // No user ID for failed attempts
                    .identifier(identifier)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(false)
                    .failureReason(failureReason)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged failed login attempt for identifier: {}", identifier);
        } catch (Exception e) {
            log.error("Failed to log login failure event", e);
            // Don't throw exception - logging failure shouldn't break authentication flow
        }
    }

    /**
     * Log a token validation event
     */
    @Async
    public void logTokenValidation(Long userId, String identifier, boolean isValid, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType(isValid ? "TOKEN_VALIDATION" : "TOKEN_INVALID")
                    .userId(userId)
                    .identifier(identifier)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(isValid)
                    .failureReason(isValid ? null : "TOKEN_INVALID")
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged token validation for user: {}, valid: {}", userId, isValid);
        } catch (Exception e) {
            log.error("Failed to log token validation event", e);
            // Don't throw exception - logging failure shouldn't break authentication flow
        }
    }

    /**
     * Log a logout event
     */
    @Async
    public void logLogout(Long userId, String identifier, String sessionId, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("LOGOUT")
                    .userId(userId)
                    .identifier(identifier)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(sessionId)
                    .requestId(getRequestId(request))
                    .success(true)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged logout for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to log logout event", e);
            // Don't throw exception - logging failure shouldn't break authentication flow
        }
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
     * Log a password reset request event
     */
    @Async
    public void logPasswordResetRequested(Long userId, String email, boolean success, String failureReason,
            HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("PASSWORD_RESET_REQUESTED")
                    .userId(userId)
                    .identifier(email)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged password reset request for email: {}, success: {}", email, success);
        } catch (Exception e) {
            log.error("Failed to log password reset request event", e);
            // Don't throw exception - logging failure shouldn't break password reset flow
        }
    }

    /**
     * Log a password reset completion event
     */
    @Async
    public void logPasswordResetCompleted(Long userId, String email, boolean success, String failureReason,
            HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("PASSWORD_RESET_COMPLETED")
                    .userId(userId)
                    .identifier(email)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .requestId(getRequestId(request))
                    .success(success)
                    .failureReason(failureReason)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged password reset completion for user: {}, success: {}", userId, success);
        } catch (Exception e) {
            log.error("Failed to log password reset completion event", e);
            // Don't throw exception - logging failure shouldn't break password reset flow
        }
    }

    /**
     * Log a session creation event
     */
    @Async
    public void logSessionCreated(Long userId, String sessionId, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("SESSION_CREATED")
                    .userId(userId)
                    .identifier(null)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(sessionId)
                    .requestId(getRequestId(request))
                    .success(true)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged session creation for user: {}, sessionId: {}", userId, sessionId);
        } catch (Exception e) {
            log.error("Failed to log session creation event", e);
            // Don't throw exception - logging failure shouldn't break session flow
        }
    }

    /**
     * Log a session revocation event
     */
    @Async
    public void logSessionRevoked(Long userId, String sessionId, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("SESSION_REVOKED")
                    .userId(userId)
                    .identifier(null)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(sessionId)
                    .requestId(getRequestId(request))
                    .success(true)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged session revocation for user: {}, sessionId: {}", userId, sessionId);
        } catch (Exception e) {
            log.error("Failed to log session revocation event", e);
            // Don't throw exception - logging failure shouldn't break session flow
        }
    }

    /**
     * Log all sessions revoked event
     */
    @Async
    public void logAllSessionsRevoked(Long userId, String currentSessionId, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("ALL_SESSIONS_REVOKED")
                    .userId(userId)
                    .identifier(null)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(currentSessionId)
                    .requestId(getRequestId(request))
                    .success(true)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged all sessions revoked for user: {}, currentSessionId: {}", userId, currentSessionId);
        } catch (Exception e) {
            log.error("Failed to log all sessions revoked event", e);
            // Don't throw exception - logging failure shouldn't break session flow
        }
    }

    /**
     * Log a successful token refresh event
     */
    @Async
    public void logTokenRefreshSuccess(Long userId, String identifier, String refreshToken,
            LocalDateTime tokenExpiresAt, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("TOKEN_REFRESH_SUCCESS")
                    .userId(userId)
                    .identifier(identifier)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(refreshToken) // refreshToken is the sessionId
                    .requestId(getRequestId(request))
                    .success(true)
                    .tokenExpiresAt(tokenExpiresAt)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged successful token refresh for user: {}, refreshToken: {}", userId, refreshToken);
        } catch (Exception e) {
            log.error("Failed to log token refresh success event", e);
            // Don't throw exception - logging failure shouldn't break token refresh flow
        }
    }

    /**
     * Log a failed token refresh event
     */
    @Async
    public void logTokenRefreshFailure(String refreshToken, String failureReason, HttpServletRequest request) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .eventType("TOKEN_REFRESH_FAILURE")
                    .userId(null) // May not have user ID if session not found
                    .identifier(null)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(refreshToken) // refreshToken is the sessionId
                    .requestId(getRequestId(request))
                    .success(false)
                    .failureReason(failureReason)
                    .build();

            authLogRepository.save(authLog);
            log.debug("Logged failed token refresh for refreshToken: {}, reason: {}", refreshToken, failureReason);
        } catch (Exception e) {
            log.error("Failed to log token refresh failure event", e);
            // Don't throw exception - logging failure shouldn't break token refresh flow
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
