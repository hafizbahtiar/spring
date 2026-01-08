package com.hafizbahtiar.spring.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk delete operations.
 * Used across all portfolio entities (Education, Experience, Project, Skill, Testimonial).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteRequest {

    /**
     * List of entity IDs to delete.
     * Must not be null or empty.
     */
    @NotNull(message = "IDs list cannot be null")
    @NotEmpty(message = "IDs list cannot be empty")
    private List<Long> ids;
}

