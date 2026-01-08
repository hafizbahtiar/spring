package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis metrics response.
 * Note: Currently not implemented (Redis not used), returns null/empty values.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisMetrics {
    private Boolean connected;
    private RedisMemory memory;
    private RedisConnections connections;
    private RedisCommands commands;
    private RedisKeyspace keyspace;
    private Long latency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisMemory {
        private Long used;
        private Long peak;
        private Long total;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisConnections {
        private Long connected;
        private Long rejected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisCommands {
        private Long processed;
        private Long total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisKeyspace {
        private Long keys;
        private Long expires;
    }
}
