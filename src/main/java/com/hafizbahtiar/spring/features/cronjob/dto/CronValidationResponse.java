package com.hafizbahtiar.spring.features.cronjob.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for cron expression validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronValidationResponse {

    /**
     * Whether the cron expression is valid
     */
    private Boolean valid;

    /**
     * Error message if validation fails
     */
    private String error;

    /**
     * Next execution times (if valid)
     * Shows the next 5-10 scheduled execution times
     */
    private List<LocalDateTime> nextExecutions;
}
