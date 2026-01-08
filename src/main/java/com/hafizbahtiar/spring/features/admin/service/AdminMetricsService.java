package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.SystemMetricsResponse;

/**
 * Service interface for admin metrics.
 */
public interface AdminMetricsService {

    /**
     * Get overall system metrics.
     * Includes metrics for queues, Redis, MongoDB, and API.
     *
     * @return SystemMetricsResponse containing all system metrics
     */
    SystemMetricsResponse getSystemMetrics();
}
