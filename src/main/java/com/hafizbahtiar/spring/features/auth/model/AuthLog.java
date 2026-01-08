package com.hafizbahtiar.spring.features.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for authentication event logging.
 * Stores authentication-related events like login attempts, successful logins,
 * and token validations.
 */
@Document(collection = "auth_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLog {

    @Id
    private String id;

    /**
     * Type of authentication event
     * Values: LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_VALIDATION, TOKEN_INVALID
     */
    @Indexed
    private String eventType;

    /**
     * User ID associated with the event (null for failed login attempts)
     */
    @Indexed
    private Long userId;

    /**
     * Email or username used in the authentication attempt
     */
    private String identifier;

    /**
     * Timestamp when the event occurred
     */
    @Indexed
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
    @Indexed
    private String sessionId;

    /**
     * Request ID for tracing
     */
    @Indexed
    private String requestId;

    /**
     * Success status of the authentication attempt
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     * Values: INVALID_CREDENTIALS, USER_NOT_FOUND, PASSWORD_MISMATCH,
     * TOKEN_EXPIRED, TOKEN_INVALID
     */
    private String failureReason;

    /**
     * Additional metadata as flexible JSON structure
     */
    private Object metadata;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;

    /**
     * Token expiration time (for login events)
     */
    private LocalDateTime tokenExpiresAt;
}
