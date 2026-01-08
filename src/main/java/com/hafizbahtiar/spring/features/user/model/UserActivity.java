package com.hafizbahtiar.spring.features.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for user activity logging.
 * Stores user activity events like registration, profile updates, API calls,
 * etc.
 */
@Document(collection = "user_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    @Id
    private String id;

    /**
     * User ID associated with the activity
     */
    @Indexed
    private Long userId;

    /**
     * Session ID (if available)
     */
    @Indexed
    private String sessionId;

    /**
     * Type of activity
     * Values: REGISTRATION, PROFILE_UPDATE, PROFILE_VIEW, EMAIL_VERIFICATION,
     * DEACTIVATION, API_CALL, PASSWORD_CHANGE, etc.
     */
    @Indexed
    private String activityType;

    /**
     * Timestamp when the activity occurred
     */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * Activity details (endpoint, method, response status, etc.)
     */
    private ActivityDetails details;

    /**
     * Additional metadata as flexible JSON structure
     */
    private Object metadata;

    /**
     * Inner class for activity details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDetails {
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
         * User agent string from the request
         */
        private String userAgent;

        /**
         * IP address of the client
         */
        private String ipAddress;

        /**
         * Request ID for tracing
         */
        private String requestId;

        /**
         * Additional context-specific data
         */
        private Object additionalData;
    }
}
