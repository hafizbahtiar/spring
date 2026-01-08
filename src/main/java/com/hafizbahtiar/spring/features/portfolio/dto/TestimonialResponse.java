package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for testimonial details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponse {

    private Long id;
    private Long userId;
    private Long projectId; // Optional project reference
    private String authorName;
    private String authorTitle;
    private String authorCompany;
    private String authorImageUrl;
    private String content;
    private Integer rating;
    private String ratingStars; // Calculated: "★★★★★" format
    private Boolean isFeatured;
    private Boolean isApproved;
    private Integer displayOrder;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Inner class for summary responses (used in nested contexts)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String authorName;
        private String authorTitle;
        private String authorCompany;
        private String authorImageUrl;
        private String content;
        private Integer rating;
        private String ratingStars;
        private Boolean isFeatured;
    }
}
