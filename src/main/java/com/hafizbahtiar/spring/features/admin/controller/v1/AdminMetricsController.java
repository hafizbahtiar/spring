package com.hafizbahtiar.spring.features.admin.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.admin.dto.SystemMetricsResponse;
import com.hafizbahtiar.spring.features.admin.service.AdminMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin metrics endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminMetricsController {

    private final AdminMetricsService adminMetricsService;

    /**
     * Get overall system metrics.
     * Includes metrics for queues, Redis, MongoDB, and API.
     * Requires: OWNER/ADMIN role OR admin.metrics page READ permission
     *
     * @return SystemMetricsResponse with all system metrics
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.metrics', 'READ')")
    public ResponseEntity<ApiResponse<SystemMetricsResponse>> getSystemMetrics() {
        log.debug("GET /api/v1/admin/metrics - Getting system metrics");
        SystemMetricsResponse metrics = adminMetricsService.getSystemMetrics();
        return ResponseUtils.ok(metrics);
    }
}
