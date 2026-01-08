package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk creating permission pages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreatePageRequest {

    /**
     * List of pages to create
     */
    @NotEmpty(message = "Pages list cannot be empty")
    @Size(max = 100, message = "Cannot create more than 100 pages at once")
    @Valid
    private List<CreatePageRequest> pages;
}
