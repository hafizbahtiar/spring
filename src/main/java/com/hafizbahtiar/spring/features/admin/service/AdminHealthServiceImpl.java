package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.HealthCheckResponse;
import com.hafizbahtiar.spring.features.admin.dto.MongoDBHealth;
import com.hafizbahtiar.spring.features.admin.dto.PostgreSQLHealth;
import com.hafizbahtiar.spring.features.admin.dto.RedisHealth;
import com.hafizbahtiar.spring.features.admin.dto.SystemHealth;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * Implementation of AdminHealthService for checking system health.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminHealthServiceImpl implements AdminHealthService {

    private final DataSource dataSource;
    private final MongoTemplate mongoTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public HealthCheckResponse getSystemHealth() {
        log.debug("Getting system health status");

        SystemHealth apiHealth = getApiHealth();
        PostgreSQLHealth postgresqlHealth = getPostgreSQLHealth();
        RedisHealth redisHealth = getRedisHealth();
        MongoDBHealth mongodbHealth = getMongoDBHealth();

        return HealthCheckResponse.builder()
                .api(apiHealth)
                .postgresql(postgresqlHealth)
                .redis(redisHealth)
                .mongodb(mongodbHealth)
                .build();
    }

    @Override
    public PostgreSQLHealth getPostgreSQLHealth() {
        log.debug("Checking PostgreSQL health");

        try {
            // Check if DataSource is HikariCP
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();

                // Test connection
                boolean connected = false;
                String database = null;
                try (var connection = dataSource.getConnection()) {
                    connected = connection.isValid(2); // 2 second timeout
                    database = connection.getCatalog();
                }

                int activeConnections = poolBean.getActiveConnections();
                int idleConnections = poolBean.getIdleConnections();
                int maxConnections = hikariDataSource.getMaximumPoolSize();
                int totalConnections = poolBean.getTotalConnections();

                String status = connected ? "healthy" : "error";
                String message = connected
                        ? String.format("PostgreSQL is connected. Active: %d, Idle: %d, Total: %d/%d",
                                activeConnections, idleConnections, totalConnections, maxConnections)
                        : "PostgreSQL connection failed";

                return PostgreSQLHealth.builder()
                        .status(status)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .connected(connected)
                        .database(database)
                        .activeConnections(activeConnections)
                        .idleConnections(idleConnections)
                        .maxConnections(maxConnections)
                        .build();
            } else {
                // Fallback for non-HikariCP DataSource
                boolean connected = false;
                String database = null;
                try (var connection = dataSource.getConnection()) {
                    connected = connection.isValid(2);
                    database = connection.getCatalog();
                }

                String status = connected ? "healthy" : "error";
                String message = connected
                        ? "PostgreSQL is connected"
                        : "PostgreSQL connection failed";

                return PostgreSQLHealth.builder()
                        .status(status)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .connected(connected)
                        .database(database)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error checking PostgreSQL health: {}", e.getMessage(), e);
            return PostgreSQLHealth.builder()
                    .status("error")
                    .message("PostgreSQL health check failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .connected(false)
                    .build();
        }
    }

    @Override
    public RedisHealth getRedisHealth() {
        log.debug("Checking Redis health");

        try {
            long startTime = System.currentTimeMillis();

            // Test Redis connection by executing PING command
            var connection = redisConnectionFactory.getConnection();
            try {
                String response = connection.ping();
                long latency = System.currentTimeMillis() - startTime;

                // Extract host and port from connection factory
                // Default values - can be configured via application.properties
                String host = "127.0.0.1";
                int port = 6379;

                boolean connected = "PONG".equals(response);
                boolean healthy = connected && latency < 100; // Consider healthy if latency < 100ms

                String status = healthy ? "healthy" : (connected ? "warning" : "error");
                String message = connected
                        ? String.format("Redis is connected. Latency: %dms", latency)
                        : "Redis connection failed";

                return RedisHealth.builder()
                        .status(status)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .connected(connected)
                        .healthy(healthy)
                        .host(host)
                        .port(port)
                        .latency(latency)
                        .build();
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            log.error("Error checking Redis health: {}", e.getMessage(), e);
            return RedisHealth.builder()
                    .status("error")
                    .message("Redis health check failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .connected(false)
                    .healthy(false)
                    .build();
        }
    }

    @Override
    public MongoDBHealth getMongoDBHealth() {
        log.debug("Checking MongoDB health");

        try {
            // Test MongoDB connection by executing a simple command
            String databaseName = mongoTemplate.getDb().getName();
            mongoTemplate.getDb().runCommand(org.bson.Document.parse("{ ping: 1 }"));

            return MongoDBHealth.builder()
                    .status("healthy")
                    .message("MongoDB is connected")
                    .timestamp(LocalDateTime.now())
                    .connected(true)
                    .database(databaseName)
                    .build();
        } catch (Exception e) {
            log.error("Error checking MongoDB health: {}", e.getMessage(), e);
            return MongoDBHealth.builder()
                    .status("error")
                    .message("MongoDB health check failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .connected(false)
                    .build();
        }
    }

    /**
     * Get API health status.
     * This is a simple check that the API is responding.
     *
     * @return SystemHealth for the API
     */
    private SystemHealth getApiHealth() {
        return SystemHealth.builder()
                .status("healthy")
                .message("API server is responding")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
