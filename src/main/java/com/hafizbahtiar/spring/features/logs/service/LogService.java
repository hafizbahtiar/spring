package com.hafizbahtiar.spring.features.logs.service;

import com.hafizbahtiar.spring.features.logs.dto.LogResponse;

import java.util.List;

/**
 * Service interface for aggregating logs from multiple MongoDB collections.
 */
public interface LogService {

    /**
     * Get aggregated logs from all collections.
     *
     * @param limit Maximum number of logs to return
     * @return List of unified log responses, sorted by timestamp descending
     */
    List<LogResponse> getAllLogs(int limit);

    /**
     * Get user activity logs.
     *
     * @param limit Maximum number of logs to return
     * @return List of user activity logs, sorted by timestamp descending
     */
    List<LogResponse> getUserActivityLogs(int limit);

    /**
     * Get security logs (auth logs + permission logs).
     *
     * @param limit Maximum number of logs to return
     * @return List of security-related logs, sorted by timestamp descending
     */
    List<LogResponse> getSecurityLogs(int limit);

    /**
     * Get portfolio logs.
     *
     * @param limit Maximum number of logs to return
     * @return List of portfolio logs, sorted by timestamp descending
     */
    List<LogResponse> getPortfolioLogs(int limit);
}
