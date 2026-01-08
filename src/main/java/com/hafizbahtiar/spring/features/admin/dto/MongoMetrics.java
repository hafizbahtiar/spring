package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MongoDB metrics response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MongoMetrics {
    private Boolean connected;
    private ConnectionPool connectionPool;
    private Integer collections;
    private List<String> databases;
    private ServerStatus serverStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionPool {
        private Integer current;
        private Integer available;
        private Integer max;
        private Integer min;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerStatus {
        private Long uptime;
        private String version;
    }
}
