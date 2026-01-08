package com.hafizbahtiar.spring.features.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cron job execution record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronJobExecution {
    private String jobName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;
    private Boolean success;
    private String error;
    private Long duration; // in milliseconds
}
