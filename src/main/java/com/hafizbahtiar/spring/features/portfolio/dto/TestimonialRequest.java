package com.hafizbahtiar.spring.features.portfolio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a testimonial.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialRequest {

    @NotBlank(message = "Author name is required")
    @Size(max = 200, message = "Author name must not exceed 200 characters")
    private String authorName;

    @Size(max = 200, message = "Author title must not exceed 200 characters")
    private String authorTitle;

    @Size(max = 200, message = "Author company must not exceed 200 characters")
    private String authorCompany;

    @Size(max = 500, message = "Author image URL must not exceed 500 characters")
    private String authorImageUrl;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private Long projectId; // Optional project reference

    private Boolean isFeatured = false;

    private Boolean isApproved = false;

    private Integer displayOrder;
}
