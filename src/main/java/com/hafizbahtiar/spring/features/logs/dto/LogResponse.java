package com.hafizbahtiar.spring.features.logs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified response DTO for aggregated logs from all MongoDB collections.
 * Represents logs from auth_logs, user_activity, portfolio_logs,
 * permission_logs,
 * payment_logs, subscription_logs, and ip_lookup_logs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponse {

    /**
     * Log ID (MongoDB document ID)
     */
    private String id;

    /**
     * Log type/source collection
     * Values: AUTH, USER_ACTIVITY, PORTFOLIO, PERMISSION, PAYMENT, SUBSCRIPTION,
     * IP_LOOKUP
     */
    private String logType;

    /**
     * Event type (from the original log document)
     */
    private String eventType;

    /**
     * User ID associated with the log (nullable)
     */
    private Long userId;

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
     * Success status of the operation
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     */
    private String failureReason;

    /**
     * Additional metadata as flexible JSON structure
     * Contains type-specific fields (e.g., entityType, entityId for portfolio logs,
     * provider for payment logs, etc.)
     */
    private Object metadata;

    /**
     * Response time in milliseconds (if available)
     */
    private Long responseTimeMs;
}
