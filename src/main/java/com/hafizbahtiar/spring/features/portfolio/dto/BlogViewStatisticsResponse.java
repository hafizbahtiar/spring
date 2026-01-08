package com.hafizbahtiar.spring.features.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for blog view statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogViewStatisticsResponse {

    /**
     * Blog post ID
     */
    private Long blogId;

    /**
     * Total view count
     */
    private Long viewCount;

    /**
     * Unique IP address view count
     */
    private Long uniqueViewCount;
}
