package com.hafizbahtiar.spring.features.admin.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.admin.dto.HealthCheckResponse;
import com.hafizbahtiar.spring.features.admin.dto.MongoDBHealth;
import com.hafizbahtiar.spring.features.admin.dto.PostgreSQLHealth;
import com.hafizbahtiar.spring.features.admin.dto.RedisHealth;
import com.hafizbahtiar.spring.features.admin.service.AdminHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin health check endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminHealthController {

    private final AdminHealthService adminHealthService;

    /**
     * Get overall system health status.
     * Includes health checks for API, PostgreSQL, Redis, and MongoDB.
     * Requires: OWNER/ADMIN role OR admin.health page READ permission
     *
     * @return HealthCheckResponse with status of all components
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.health', 'READ')")
    public ResponseEntity<ApiResponse<HealthCheckResponse>> getSystemHealth() {
        log.debug("GET /api/v1/admin/health - Getting system health");
        HealthCheckResponse health = adminHealthService.getSystemHealth();
        return ResponseUtils.ok(health);
    }

    /**
     * Get PostgreSQL database health status.
     * Requires: OWNER/ADMIN role OR admin.health page READ permission
     *
     * @return PostgreSQLHealth with connection status and metrics
     */
    @GetMapping("/health/postgresql")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.health', 'READ')")
    public ResponseEntity<ApiResponse<PostgreSQLHealth>> getPostgreSQLHealth() {
        log.debug("GET /api/v1/admin/health/postgresql - Getting PostgreSQL health");
        PostgreSQLHealth health = adminHealthService.getPostgreSQLHealth();
        return ResponseUtils.ok(health);
    }

    /**
     * Get Redis health status.
     * Requires: OWNER/ADMIN role OR admin.health page READ permission
     *
     * @return RedisHealth with connection status, latency, and server info
     */
    @GetMapping("/health/redis")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.health', 'READ')")
    public ResponseEntity<ApiResponse<RedisHealth>> getRedisHealth() {
        log.debug("GET /api/v1/admin/health/redis - Getting Redis health");
        RedisHealth health = adminHealthService.getRedisHealth();
        return ResponseUtils.ok(health);
    }

    /**
     * Get MongoDB database health status.
     * Requires: OWNER/ADMIN role OR admin.health page READ permission
     *
     * @return MongoDBHealth with connection status and database info
     */
    @GetMapping("/health/mongodb")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'admin', 'admin.health', 'READ')")
    public ResponseEntity<ApiResponse<MongoDBHealth>> getMongoDBHealth() {
        log.debug("GET /api/v1/admin/health/mongodb - Getting MongoDB health");
        MongoDBHealth health = adminHealthService.getMongoDBHealth();
        return ResponseUtils.ok(health);
    }
}
