/**
 * Logs feature module.
 * 
 * <p>
 * This module provides aggregated log viewing capabilities for administrators.
 * It aggregates logs from multiple MongoDB collections (auth_logs,
 * user_activity,
 * portfolio_logs, permission_logs) into a unified view.
 * 
 * <p>
 * Key components:
 * <ul>
 * <li>{@link com.hafizbahtiar.spring.features.logs.dto.LogResponse} - Unified
 * DTO for all log types</li>
 * <li>{@link com.hafizbahtiar.spring.features.logs.service.LogService} -
 * Service for aggregating logs</li>
 * <li>{@link com.hafizbahtiar.spring.features.logs.controller.v1.LogController}
 * - REST API endpoints</li>
 * </ul>
 * 
 * <p>
 * Authorization: All endpoints require OWNER or ADMIN role.
 * 
 * @author Hafiz Bahtiar
 * @version 1.0
 */
package com.hafizbahtiar.spring.features.logs;
