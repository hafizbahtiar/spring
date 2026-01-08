package com.hafizbahtiar.spring.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for bulk delete operations.
 * Used across all portfolio entities (Education, Experience, Project, Skill, Testimonial).
 * 
 * Contains information about successful deletions and failures.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteResponse {

    /**
     * Number of successfully deleted items.
     */
    private Integer deletedCount;

    /**
     * List of IDs that failed to delete.
     * Empty list if all deletions were successful.
     */
    @Builder.Default
    private List<Long> failedIds = new ArrayList<>();

    /**
     * Create a successful bulk delete response.
     *
     * @param deletedCount Number of successfully deleted items
     * @return BulkDeleteResponse with no failed IDs
     */
    public static BulkDeleteResponse success(int deletedCount) {
        return BulkDeleteResponse.builder()
                .deletedCount(deletedCount)
                .failedIds(new ArrayList<>())
                .build();
    }

    /**
     * Create a bulk delete response with failures.
     *
     * @param deletedCount Number of successfully deleted items
     * @param failedIds    List of IDs that failed to delete
     * @return BulkDeleteResponse with failed IDs
     */
    public static BulkDeleteResponse withFailures(int deletedCount, List<Long> failedIds) {
        return BulkDeleteResponse.builder()
                .deletedCount(deletedCount)
                .failedIds(failedIds != null ? failedIds : new ArrayList<>())
                .build();
    }
}

