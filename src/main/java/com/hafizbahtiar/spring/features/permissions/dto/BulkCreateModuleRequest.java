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
 * DTO for bulk creating permission modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateModuleRequest {

    /**
     * List of modules to create
     */
    @NotEmpty(message = "Modules list cannot be empty")
    @Size(max = 100, message = "Cannot create more than 100 modules at once")
    @Valid
    private List<CreateModuleRequest> modules;
}
