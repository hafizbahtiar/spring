package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for blog view history entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogViewResponse {

    /**
     * View ID
     */
    private Long id;

    /**
     * Blog post ID
     */
    private Long blogId;

    /**
     * IP address of the viewer
     */
    private String ipAddress;

    /**
     * User agent string
     */
    private String userAgent;

    /**
     * User ID if viewer was authenticated (optional)
     */
    private Long userId;

    /**
     * Timestamp when the view occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime viewedAt;
}
