package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a permission group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {

    /**
     * Group name (must be unique)
     */
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    /**
     * Group description
     */
    private String description;

    /**
     * Whether the group is active (default: true)
     */
    @Builder.Default
    private Boolean active = true;
}
