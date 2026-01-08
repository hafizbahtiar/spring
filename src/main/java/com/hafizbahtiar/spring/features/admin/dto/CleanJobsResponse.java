package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clean jobs response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanJobsResponse {
    private String queueName;
    private String status; // "completed", "failed", "all"
    private Long cleaned;
}
