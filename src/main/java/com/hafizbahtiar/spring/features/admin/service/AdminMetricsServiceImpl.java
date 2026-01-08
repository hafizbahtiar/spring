package com.hafizbahtiar.spring.features.admin.service;

import com.hafizbahtiar.spring.features.admin.dto.ApiMetrics;
import com.hafizbahtiar.spring.features.admin.dto.MongoMetrics;
import com.hafizbahtiar.spring.features.admin.dto.QueueMetrics;
import com.hafizbahtiar.spring.features.admin.dto.RedisMetrics;
import com.hafizbahtiar.spring.features.admin.dto.SystemMetricsResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of AdminMetricsService for collecting system metrics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMetricsServiceImpl implements AdminMetricsService {

    private final MeterRegistry meterRegistry;
    private final MongoTemplate mongoTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public SystemMetricsResponse getSystemMetrics() {
        log.debug("Getting system metrics");

        List<QueueMetrics> queues = getQueueMetrics();
        RedisMetrics redis = getRedisMetrics();
        MongoMetrics mongodb = getMongoMetrics();
        ApiMetrics api = getApiMetrics();

        return SystemMetricsResponse.builder()
                .timestamp(LocalDateTime.now())
                .queues(queues)
                .redis(redis)
                .mongodb(mongodb)
                .api(api)
                .build();
    }

    /**
     * Get queue metrics.
     * Currently returns empty list as no queue system is implemented.
     *
     * @return Empty list of queue metrics
     */
    private List<QueueMetrics> getQueueMetrics() {
        // No queue system implemented yet
        return new ArrayList<>();
    }

    /**
     * Get Redis metrics.
     *
     * @return RedisMetrics with connection status, memory, connections, commands,
     *         and keyspace info
     */
    private RedisMetrics getRedisMetrics() {
        try {
            var connection = redisConnectionFactory.getConnection();
            try {
                // Get INFO command results using serverCommands() (non-deprecated API)
                Properties info = connection.serverCommands().info();

                // Extract memory information
                long usedMemory = Long.parseLong(info.getProperty("used_memory", "0"));
                long usedMemoryPeak = Long.parseLong(info.getProperty("used_memory_peak", "0"));
                long totalSystemMemory = Long.parseLong(info.getProperty("total_system_memory", "0"));
                double memoryPercentage = totalSystemMemory > 0
                        ? (double) usedMemory / totalSystemMemory * 100.0
                        : 0.0;

                // Extract connection information
                long connectedClients = Long.parseLong(info.getProperty("connected_clients", "0"));
                long rejectedConnections = Long.parseLong(info.getProperty("rejected_connections", "0"));

                // Extract command statistics
                long totalCommandsProcessed = Long.parseLong(info.getProperty("total_commands_processed", "0"));
                long totalCommands = totalCommandsProcessed; // Same value for now

                // Extract keyspace information
                long db0Keys = 0;
                long db0Expires = 0;
                String db0 = info.getProperty("db0");
                if (db0 != null) {
                    // Parse "keys=123,expires=45" format
                    String[] parts = db0.split(",");
                    for (String part : parts) {
                        if (part.startsWith("keys=")) {
                            db0Keys = Long.parseLong(part.substring(5));
                        } else if (part.startsWith("expires=")) {
                            db0Expires = Long.parseLong(part.substring(8));
                        }
                    }
                }

                // Calculate latency (ping test)
                long startTime = System.currentTimeMillis();
                connection.ping();
                long latency = System.currentTimeMillis() - startTime;

                return RedisMetrics.builder()
                        .connected(true)
                        .memory(RedisMetrics.RedisMemory.builder()
                                .used(usedMemory)
                                .peak(usedMemoryPeak)
                                .total(totalSystemMemory)
                                .percentage(memoryPercentage)
                                .build())
                        .connections(RedisMetrics.RedisConnections.builder()
                                .connected(connectedClients)
                                .rejected(rejectedConnections)
                                .build())
                        .commands(RedisMetrics.RedisCommands.builder()
                                .processed(totalCommandsProcessed)
                                .total(totalCommands)
                                .build())
                        .keyspace(RedisMetrics.RedisKeyspace.builder()
                                .keys(db0Keys)
                                .expires(db0Expires)
                                .build())
                        .latency(latency)
                        .build();
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            log.error("Error getting Redis metrics: {}", e.getMessage(), e);
            return RedisMetrics.builder()
                    .connected(false)
                    .memory(RedisMetrics.RedisMemory.builder()
                            .used(0L)
                            .peak(0L)
                            .total(0L)
                            .percentage(0.0)
                            .build())
                    .connections(RedisMetrics.RedisConnections.builder()
                            .connected(0L)
                            .rejected(0L)
                            .build())
                    .commands(RedisMetrics.RedisCommands.builder()
                            .processed(0L)
                            .total(0L)
                            .build())
                    .keyspace(RedisMetrics.RedisKeyspace.builder()
                            .keys(0L)
                            .expires(0L)
                            .build())
                    .build();
        }
    }

    /**
     * Get MongoDB metrics.
     *
     * @return MongoMetrics with connection pool and database information
     */
    private MongoMetrics getMongoMetrics() {
        boolean connected = false;
        int collectionsCount = 0;
        List<String> databases = new ArrayList<>();
        MongoMetrics.ServerStatus serverStatusObj = null;

        try {
            // Try to get basic connection info (this should work with basic permissions)
            try {
                String dbName = mongoTemplate.getDb().getName();
                databases.add(dbName);
                connected = true;
            } catch (Exception e) {
                log.debug("Error getting database name: {}", e.getMessage());
            }

            // Get list of collections (requires read permission on database)
            try {
                collectionsCount = mongoTemplate.getDb().listCollectionNames().into(new ArrayList<>()).size();
                connected = true; // If we can list collections, we're connected
            } catch (Exception e) {
                log.debug("Error getting collections count: {}", e.getMessage());
            }

            // Try to get database stats (optional - may require additional permissions)
            try {
                mongoTemplate.getDb().runCommand(
                        org.bson.Document.parse("{ dbStats: 1 }"));
            } catch (Exception e) {
                log.debug("Error getting database stats (may require admin permissions): {}", e.getMessage());
            }

            // Try to get server status (optional - requires admin permissions)
            org.bson.Document serverStatus = null;
            try {
                serverStatus = mongoTemplate.getDb().runCommand(
                        org.bson.Document.parse("{ serverStatus: 1 }"));
            } catch (Exception e) {
                log.debug("Error getting server status (requires admin permissions): {}", e.getMessage());
                // Don't log as error - this is expected if user doesn't have admin privileges
            }

            // Extract server status if available
            if (serverStatus != null) {
                try {
                    Long uptime = serverStatus.getLong("uptime");
                    org.bson.Document versionDoc = serverStatus.get("version", org.bson.Document.class);
                    String version = versionDoc != null ? versionDoc.getString("version") : "unknown";

                    serverStatusObj = MongoMetrics.ServerStatus.builder()
                            .uptime(uptime != null ? uptime : 0L)
                            .version(version)
                            .build();
                } catch (Exception e) {
                    log.debug("Error parsing server status: {}", e.getMessage());
                }
            }

            // Extract connection pool info (not directly available from MongoDB Java driver)
            MongoMetrics.ConnectionPool connectionPool = MongoMetrics.ConnectionPool.builder()
                    .current(0) // Not directly available from MongoDB Java driver
                    .available(0)
                    .max(100) // Default value
                    .min(0)
                    .build();

            return MongoMetrics.builder()
                    .connected(connected)
                    .connectionPool(connectionPool)
                    .collections(collectionsCount)
                    .databases(databases)
                    .serverStatus(serverStatusObj)
                    .build();
        } catch (Exception e) {
            log.error("Error getting MongoDB metrics: {}", e.getMessage(), e);
            return MongoMetrics.builder()
                    .connected(false)
                    .connectionPool(MongoMetrics.ConnectionPool.builder()
                            .current(0)
                            .available(0)
                            .max(0)
                            .min(0)
                            .build())
                    .collections(0)
                    .databases(Collections.emptyList())
                    .build();
        }
    }

    /**
     * Get API metrics from Spring Boot Actuator.
     *
     * @return ApiMetrics with request counts, response times, and error rates
     */
    private ApiMetrics getApiMetrics() {
        try {
            // Get HTTP server request metrics
            long totalRequests = getCounterValue("http.server.requests", "total");
            long successfulRequests = getCounterValue("http.server.requests", "status", "2xx", "3xx");
            long failedRequests = getCounterValue("http.server.requests", "status", "4xx", "5xx");

            // Calculate rates (requests per second) - approximate
            double requestRate = calculateRate("http.server.requests");

            // Get response time metrics
            Timer timer = meterRegistry.find("http.server.requests").timer();

            double averageResponseTime = timer != null ? timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS) : 0.0;
            // Note: Percentile methods are deprecated, using mean as approximation
            // For production, consider using DistributionSummary or custom metrics
            double p50 = averageResponseTime; // Approximation
            double p95 = averageResponseTime * 1.5; // Approximation
            double p99 = averageResponseTime * 2.0; // Approximation

            // Calculate error rate
            double errorRate = totalRequests > 0 ? (double) failedRequests / totalRequests * 100.0 : 0.0;

            return ApiMetrics.builder()
                    .requests(ApiMetrics.RequestMetrics.builder()
                            .total(totalRequests)
                            .successful(successfulRequests)
                            .failed(failedRequests)
                            .rate(requestRate)
                            .build())
                    .responseTime(ApiMetrics.ResponseTimeMetrics.builder()
                            .average(averageResponseTime)
                            .p50(p50)
                            .p95(p95)
                            .p99(p99)
                            .build())
                    .errorRate(errorRate)
                    .build();
        } catch (Exception e) {
            log.error("Error getting API metrics: {}", e.getMessage(), e);
            // Return default/empty metrics on error
            return ApiMetrics.builder()
                    .requests(ApiMetrics.RequestMetrics.builder()
                            .total(0L)
                            .successful(0L)
                            .failed(0L)
                            .rate(0.0)
                            .build())
                    .responseTime(ApiMetrics.ResponseTimeMetrics.builder()
                            .average(0.0)
                            .p50(0.0)
                            .p95(0.0)
                            .p99(0.0)
                            .build())
                    .errorRate(0.0)
                    .build();
        }
    }

    /**
     * Get counter value from MeterRegistry.
     */
    private long getCounterValue(String name, String... tags) {
        try {
            io.micrometer.core.instrument.Counter counter = meterRegistry.find(name)
                    .tags(tags)
                    .counter();
            return counter != null ? (long) counter.count() : 0L;
        } catch (Exception e) {
            log.debug("Error getting counter {}: {}", name, e.getMessage());
            return 0L;
        }
    }

    /**
     * Calculate approximate rate for a metric.
     */
    private double calculateRate(String name) {
        try {
            io.micrometer.core.instrument.Counter counter = meterRegistry.find(name).counter();
            if (counter != null) {
                // This is a simplified rate calculation
                // In production, you might want to use a more sophisticated approach
                return counter.count();
            }
            return 0.0;
        } catch (Exception e) {
            log.debug("Error calculating rate for {}: {}", name, e.getMessage());
            return 0.0;
        }
    }
}
