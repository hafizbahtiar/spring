package com.hafizbahtiar.spring.features.ipaddress.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for IP address lookup event logging.
 * Stores IP geolocation lookup events for audit and security purposes.
 */
@Document(collection = "ip_lookup_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPLookupLog {

    @Id
    private String id;

    /**
     * Type of lookup event
     * Values: IP_LOOKUP, SESSION_IP_LOOKUP
     */
    @Indexed
    private String eventType;

    /**
     * User ID who performed the lookup (owner/admin)
     */
    @Indexed
    private Long userId;

    /**
     * IP address that was looked up
     */
    @Indexed
    private String lookedUpIp;

    /**
     * Session ID (if lookup was for a session)
     */
    @Indexed
    private String sessionId;

    /**
     * Timestamp when the lookup occurred
     */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * IP address of the client making the lookup request
     */
    private String clientIpAddress;

    /**
     * User agent string from the request
     */
    private String userAgent;

    /**
     * Request ID for tracing
     */
    @Indexed
    private String requestId;

    /**
     * Success status of the lookup
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     * Values: INVALID_IP, IP_NOT_FOUND, LOOKUP_FAILED, SESSION_NOT_FOUND
     */
    private String failureReason;

    /**
     * Geolocation data result (if successful)
     * Stores country, city, region, etc.
     */
    private Object geolocationData;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;
}
