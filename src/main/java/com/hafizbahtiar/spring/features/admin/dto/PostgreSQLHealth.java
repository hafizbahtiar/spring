package com.hafizbahtiar.spring.features.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PostgreSQL database health status response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgreSQLHealth {
    private String status; // "healthy", "warning", "error"
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private Boolean connected;
    private String database;
    private Integer activeConnections;
    private Integer idleConnections;
    private Integer maxConnections;
}
