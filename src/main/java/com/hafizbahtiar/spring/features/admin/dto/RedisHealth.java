package com.hafizbahtiar.spring.features.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Redis health status response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisHealth {
    private String status; // "healthy", "warning", "error"
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private Boolean connected;
    private Boolean healthy;
    private String host;
    private Integer port;
    private Long latency; // in milliseconds
}
