package com.hafizbahtiar.spring.features.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a blog post.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {

    /**
     * Blog post title
     */
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /**
     * URL-friendly slug (optional, auto-generated from title if not provided)
     */
    @Size(max = 200, message = "Slug must not exceed 200 characters")
    private String slug;

    /**
     * Blog post content (HTML or Markdown)
     */
    @NotBlank(message = "Content is required")
    private String content;

    /**
     * Short excerpt/description
     */
    @Size(max = 500, message = "Excerpt must not exceed 500 characters")
    private String excerpt;

    /**
     * Cover image URL
     */
    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImage;

    /**
     * Whether this blog post is published
     */
    private Boolean published = false;

    /**
     * Tags (list of strings)
     */
    private List<String> tags;
}
