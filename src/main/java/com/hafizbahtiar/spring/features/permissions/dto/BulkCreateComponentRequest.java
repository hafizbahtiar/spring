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
 * DTO for bulk creating permission components.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateComponentRequest {

    /**
     * List of components to create
     */
    @NotEmpty(message = "Components list cannot be empty")
    @Size(max = 100, message = "Cannot create more than 100 components at once")
    @Valid
    private List<CreateComponentRequest> components;
}
