package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cron job execution history response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronJobHistoryResponse {
    private String jobName;
    private List<CronJobExecution> history;
    private Long total;
}
