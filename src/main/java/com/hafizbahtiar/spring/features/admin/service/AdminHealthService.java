package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.HealthCheckResponse;
import com.hafizbahtiar.spring.features.admin.dto.MongoDBHealth;
import com.hafizbahtiar.spring.features.admin.dto.PostgreSQLHealth;
import com.hafizbahtiar.spring.features.admin.dto.RedisHealth;

/**
 * Service interface for admin health checks.
 */
public interface AdminHealthService {

    /**
     * Get overall system health status.
     *
     * @return HealthCheckResponse containing health status for API, PostgreSQL,
     *         Redis, and MongoDB
     */
    HealthCheckResponse getSystemHealth();

    /**
     * Get PostgreSQL database health status.
     *
     * @return PostgreSQLHealth containing connection status and metrics
     */
    PostgreSQLHealth getPostgreSQLHealth();

    /**
     * Get Redis health status.
     *
     * @return RedisHealth containing connection status, latency, and server info
     */
    RedisHealth getRedisHealth();

    /**
     * Get MongoDB database health status.
     *
     * @return MongoDBHealth containing connection status and database info
     */
    MongoDBHealth getMongoDBHealth();
}
