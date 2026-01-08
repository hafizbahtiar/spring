package com.hafizbahtiar.spring.features.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for tracking cron job executions.
 */
@Document(collection = "cron_job_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronJobExecutionLog {
    @Id
    private String id;
    private String jobName;
    private LocalDateTime executedAt;
    private Boolean success;
    private String error;
    private Long duration; // in milliseconds
    private LocalDateTime createdAt;
}
