package com.hafizbahtiar.spring.features.permissions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MongoDB document for permission event logging.
 * Stores permission-related events like group creation, permission assignment,
 * user-group assignments, and permission checks for audit and security
 * purposes.
 */
@Document(collection = "permission_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionLog {

    @Id
    private String id;

    /**
     * Type of permission event
     * Values: GROUP_CREATED, GROUP_UPDATED, GROUP_DELETED, PERMISSION_ADDED,
     * PERMISSION_UPDATED, PERMISSION_REMOVED, USER_ASSIGNED, USER_REMOVED,
     * PERMISSION_CHECKED
     */
    @Indexed
    private String eventType;

    /**
     * User ID who performed the action
     */
    @Indexed
    private Long userId;

    /**
     * Permission group ID (if applicable)
     */
    @Indexed
    private Long groupId;

    /**
     * Permission ID (if applicable)
     */
    @Indexed
    private Long permissionId;

    /**
     * Target user ID (for user assignment/removal events)
     */
    @Indexed
    private Long targetUserId;

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
     * Additional event details
     */
    private EventDetails details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDetails {
        /**
         * API endpoint that triggered the event
         */
        private String endpoint;

        /**
         * HTTP method
         */
        private String method;

        /**
         * HTTP response status code
         */
        private Integer responseStatus;

        /**
         * Group name (for group events)
         */
        private String groupName;

        /**
         * Permission type (MODULE, PAGE, COMPONENT)
         */
        private String permissionType;

        /**
         * Resource type
         */
        private String resourceType;

        /**
         * Resource identifier
         */
        private String resourceIdentifier;

        /**
         * Permission action (READ, WRITE, DELETE, EXECUTE)
         */
        private String action;

        /**
         * Target user email (for user assignment events)
         */
        private String targetUserEmail;

        /**
         * Module key (for module events)
         */
        private String moduleKey;

        /**
         * Module name (for module events)
         */
        private String moduleName;

        /**
         * Page key (for page events)
         */
        private String pageKey;

        /**
         * Page name (for page events)
         */
        private String pageName;

        /**
         * Component key (for component events)
         */
        private String componentKey;

        /**
         * Component name (for component events)
         */
        private String componentName;

        /**
         * Additional metadata as flexible structure
         */
        private Map<String, Object> additionalInfo;
    }
}
