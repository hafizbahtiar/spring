package com.hafizbahtiar.spring.features.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Queue statistics for a single queue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStats {
    private Long waiting;
    private Long active;
    private Long completed;
    private Long failed;
    private Long delayed;
    private Long paused;
}
