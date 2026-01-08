package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Job history response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobHistoryResponse {
    private List<QueueJob> jobs;
    private Long total;
    private String status; // "completed", "failed", "active", "waiting", "delayed"
}
