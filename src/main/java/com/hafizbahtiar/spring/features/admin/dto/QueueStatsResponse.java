package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Queue statistics response for all queues.
 * Maps queue name to its statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatsResponse {
    private Map<String, QueueStats> queues;
    private QueueStatsSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueStatsSummary {
        private Long totalWaiting;
        private Long totalActive;
        private Long totalCompleted;
        private Long totalFailed;
        private Long totalDelayed;
        private Long totalPaused;
    }
}
