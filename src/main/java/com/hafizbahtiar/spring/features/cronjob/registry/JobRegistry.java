package com.hafizbahtiar.spring.features.cronjob.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Registry for predefined cron jobs.
 * Allows owners to discover and select from available job classes and methods.
 */
public interface JobRegistry {

    /**
     * Get all available predefined jobs.
     * Scans Spring context for service beans with executable methods.
     *
     * @return List of available job definitions
     */
    List<JobDefinition> getAvailableJobs();

    /**
     * Get available jobs filtered by service name.
     *
     * @param serviceName Service name (e.g., "SessionCleanupService")
     * @return List of job definitions for the service
     */
    List<JobDefinition> getJobsByService(String serviceName);

    /**
     * Get a specific job definition by service and method name.
     *
     * @param serviceName Service name
     * @param methodName  Method name
     * @return JobDefinition or null if not found
     */
    JobDefinition getJob(String serviceName, String methodName);

    /**
     * Refresh the registry (re-scan Spring context).
     * Useful when new services are added dynamically.
     */
    void refresh();

    /**
     * Job definition representing an available executable method.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class JobDefinition {
        /**
         * Service name (bean name in Spring context)
         * Example: "SessionCleanupService"
         */
        private String serviceName;

        /**
         * Service class name (fully qualified)
         * Example:
         * "com.hafizbahtiar.spring.features.auth.service.SessionCleanupService"
         */
        private String serviceClassName;

        /**
         * Method name
         * Example: "cleanupExpiredSessions"
         */
        private String methodName;

        /**
         * Full job class string (for use in CronJob.jobClass)
         * Format: "ServiceName.methodName"
         * Example: "SessionCleanupService.cleanupExpiredSessions"
         */
        private String jobClass;

        /**
         * Method description (from JavaDoc or annotation, if available)
         */
        private String description;

        /**
         * Whether the method is currently available (bean exists and method is
         * accessible)
         */
        private Boolean available;
    }
}
