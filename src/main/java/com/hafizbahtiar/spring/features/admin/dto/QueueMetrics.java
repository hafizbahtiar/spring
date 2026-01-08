package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Queue metrics response.
 * Note: Currently not implemented (no queue system), returns empty values.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMetrics {
    private String name;
    private Long waiting;
    private Long active;
    private Long completed;
    private Long failed;
    private Long delayed;
    private Long total;
    private Double successRate;
    private Double failureRate;
    private Double throughput;
}
