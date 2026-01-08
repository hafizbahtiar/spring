package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Queue job information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueJob {
    private String id;
    private String name;
    private Map<String, Object> data;
    private String failedReason;
    private Long timestamp;
    private Long processedOn;
    private Long finishedOn;
    private Integer attemptsMade;
}
