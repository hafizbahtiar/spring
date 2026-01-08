package com.hafizbahtiar.spring.features.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete system metrics response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsResponse {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private List<QueueMetrics> queues;
    private RedisMetrics redis;
    private MongoMetrics mongodb;
    private ApiMetrics api;
}
