package com.hafizbahtiar.spring.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for authentication log entries.
 * Used for retrieving authentication event logs for users and admins.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLogResponse {

    /**
     * Log entry ID
     */
    private String id;

    /**
     * Type of authentication event
     * Values: LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_VALIDATION,
     * TOKEN_INVALID,
     * TOKEN_REFRESH_SUCCESS, TOKEN_REFRESH_FAILURE, SESSION_CREATED,
     * SESSION_REVOKED,
     * PASSWORD_RESET_REQUESTED, PASSWORD_RESET_COMPLETED
     */
    private String eventType;

    /**
     * User ID associated with the event (null for failed login attempts)
     */
    private Long userId;

    /**
     * Email or username used in the authentication attempt
     */
    private String identifier;

    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;

    /**
     * IP address of the client
     */
    private String ipAddress;

    /**
     * User agent string from the request
     */
    private String userAgent;

    /**
     * Session ID (if available)
     */
    private String sessionId;

    /**
     * Request ID for tracing
     */
    private String requestId;

    /**
     * Success status of the authentication attempt
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     * Values: INVALID_CREDENTIALS, USER_NOT_FOUND, PASSWORD_MISMATCH,
     * TOKEN_EXPIRED, TOKEN_INVALID, SESSION_NOT_FOUND, etc.
     */
    private String failureReason;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;

    /**
     * Token expiration time (for login events)
     */
    private LocalDateTime tokenExpiresAt;
}
