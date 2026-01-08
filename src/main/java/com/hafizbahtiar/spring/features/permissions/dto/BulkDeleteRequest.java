package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk deleting resources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteRequest {

    /**
     * List of resource IDs to delete
     */
    @NotEmpty(message = "IDs list cannot be empty")
    @Size(max = 100, message = "Cannot delete more than 100 items at once")
    private List<@NotNull(message = "ID cannot be null") Long> ids;
}
