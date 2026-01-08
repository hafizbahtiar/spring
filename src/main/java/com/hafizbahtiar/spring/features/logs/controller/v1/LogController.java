package com.hafizbahtiar.spring.features.logs.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.features.logs.dto.LogResponse;
import com.hafizbahtiar.spring.features.logs.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for aggregated logs.
 * Provides endpoints to retrieve logs from all MongoDB collections.
 * 
 * Authorization: OWNER/ADMIN only
 */
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class LogController {

    private final LogService logService;

    /**
     * GET /api/v1/logs?type=all&limit=20
     * Get aggregated logs from all collections (admin/owner only)
     * 
     * @param type  Log type filter (optional, defaults to "all")
     * @param limit Maximum number of logs to return (optional, defaults to 20)
     * @return List of unified log responses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LogResponse>>> getAllLogs(
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        log.debug("Fetching logs with type: {} and limit: {}", type, limit);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 20; // Default to 20 if invalid
        }

        List<LogResponse> logs;

        switch (type.toUpperCase()) {
            case "ALL":
                logs = logService.getAllLogs(limit);
                break;
            case "USER_ACTIVITY":
                logs = logService.getUserActivityLogs(limit);
                break;
            case "SECURITY":
                logs = logService.getSecurityLogs(limit);
                break;
            case "PORTFOLIO":
                logs = logService.getPortfolioLogs(limit);
                break;
            default:
                logs = logService.getAllLogs(limit);
        }

        return ResponseEntity.ok(ApiResponse.<List<LogResponse>>builder()
                .success(true)
                .data(logs)
                .message("Logs retrieved successfully")
                .build());
    }

    /**
     * GET /api/v1/logs/user-activity?limit=10
     * Get user activity logs
     * 
     * @param limit Maximum number of logs to return (optional, defaults to 10)
     * @return List of user activity logs
     */
    @GetMapping("/user-activity")
    public ResponseEntity<ApiResponse<List<LogResponse>>> getUserActivityLogs(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        log.debug("Fetching user activity logs with limit: {}", limit);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 10; // Default to 10 if invalid
        }

        List<LogResponse> logs = logService.getUserActivityLogs(limit);

        return ResponseEntity.ok(ApiResponse.<List<LogResponse>>builder()
                .success(true)
                .data(logs)
                .message("User activity logs retrieved successfully")
                .build());
    }

    /**
     * GET /api/v1/logs/security?limit=10
     * Get security logs (auth logs + permission logs)
     * 
     * @param limit Maximum number of logs to return (optional, defaults to 10)
     * @return List of security-related logs
     */
    @GetMapping("/security")
    public ResponseEntity<ApiResponse<List<LogResponse>>> getSecurityLogs(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        log.debug("Fetching security logs with limit: {}", limit);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 10; // Default to 10 if invalid
        }

        List<LogResponse> logs = logService.getSecurityLogs(limit);

        return ResponseEntity.ok(ApiResponse.<List<LogResponse>>builder()
                .success(true)
                .data(logs)
                .message("Security logs retrieved successfully")
                .build());
    }

    /**
     * GET /api/v1/logs/portfolio?limit=10
     * Get portfolio logs
     * 
     * @param limit Maximum number of logs to return (optional, defaults to 10)
     * @return List of portfolio logs
     */
    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<List<LogResponse>>> getPortfolioLogs(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        log.debug("Fetching portfolio logs with limit: {}", limit);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 10; // Default to 10 if invalid
        }

        List<LogResponse> logs = logService.getPortfolioLogs(limit);

        return ResponseEntity.ok(ApiResponse.<List<LogResponse>>builder()
                .success(true)
                .data(logs)
                .message("Portfolio logs retrieved successfully")
                .build());
    }
}
