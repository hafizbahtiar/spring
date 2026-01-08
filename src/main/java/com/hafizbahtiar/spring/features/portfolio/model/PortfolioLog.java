package com.hafizbahtiar.spring.features.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for portfolio event logging.
 * Stores portfolio-related events like skill/experience/project/education
 * creation,
 * updates, deletions, reordering, and featured status changes for audit and
 * analytics purposes.
 */
@Document(collection = "portfolio_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioLog {

    @Id
    private String id;

    /**
     * User ID who owns the portfolio item
     */
    @Indexed
    private Long userId;

    /**
     * Type of portfolio entity
     * Values: SKILL, EXPERIENCE, PROJECT, EDUCATION, COMPANY, CERTIFICATION,
     * TESTIMONIAL, CONTACT
     */
    @Indexed
    private String entityType;

    /**
     * Entity ID (from PostgreSQL)
     */
    @Indexed
    private Long entityId;

    /**
     * Type of portfolio event
     * Values: CREATED, UPDATED, DELETED, REORDERED, FEATURED, UNFEATURED,
     * ACTIVATED, DEACTIVATED
     */
    @Indexed
    private String eventType;

    /**
     * Timestamp when the event occurred
     */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * IP address of the client (for API calls)
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
     * Success status of the operation
     */
    private Boolean success;

    /**
     * Failure reason (if success is false)
     */
    private String failureReason;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;

    /**
     * Event details (endpoint, method, changes, etc.)
     */
    private PortfolioEventDetails details;

    /**
     * Additional metadata as flexible JSON structure
     */
    private Object metadata;

    /**
     * Inner class for portfolio event details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioEventDetails {
        /**
         * API endpoint accessed
         */
        private String endpoint;

        /**
         * HTTP method (GET, POST, PUT, DELETE, etc.)
         */
        private String method;

        /**
         * HTTP response status code
         */
        private Integer responseStatus;

        /**
         * Response time in milliseconds
         */
        private Long responseTimeMs;

        /**
         * Entity name/title (for context)
         */
        private String entityName;

        /**
         * Previous display order (for reorder events)
         */
        private Integer previousDisplayOrder;

        /**
         * New display order (for reorder events)
         */
        private Integer newDisplayOrder;

        /**
         * Previous featured status (for featured events)
         */
        private Boolean previousFeaturedStatus;

        /**
         * New featured status (for featured events)
         */
        private Boolean newFeaturedStatus;

        /**
         * Previous active status (for activation events)
         */
        private Boolean previousActiveStatus;

        /**
         * New active status (for activation events)
         */
        private Boolean newActiveStatus;

        /**
         * Previous status (for status change events, e.g., contact status)
         */
        private String previousStatus;

        /**
         * New status (for status change events, e.g., contact status)
         */
        private String newStatus;

        /**
         * Field changes (for update events)
         */
        private Object fieldChanges;

        /**
         * Additional context-specific data
         */
        private Object additionalData;
    }
}
