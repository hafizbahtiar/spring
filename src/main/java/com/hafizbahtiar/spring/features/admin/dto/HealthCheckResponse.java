package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Complete health check response for all system components.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {
    private SystemHealth api;
    private PostgreSQLHealth postgresql;
    private RedisHealth redis;
    private MongoDBHealth mongodb;
}
