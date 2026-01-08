package com.hafizbahtiar.spring.features.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cron job status information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronJobStatus {
    private String name;
    private String cronExpression;
    private Boolean enabled;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastExecution;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextExecution;
    private Long executionCount;
    private Long successCount;
    private Long failureCount;
    private String lastError;
    private Long lastDuration; // in milliseconds
}
