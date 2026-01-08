package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API metrics response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMetrics {
    private RequestMetrics requests;
    private ResponseTimeMetrics responseTime;
    private Double errorRate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestMetrics {
        private Long total;
        private Long successful;
        private Long failed;
        private Double rate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseTimeMetrics {
        private Double average;
        private Double p50;
        private Double p95;
        private Double p99;
    }
}
