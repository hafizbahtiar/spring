package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a permission group.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequest {

    /**
     * Group name (must be unique if provided)
     */
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    /**
     * Group description
     */
    private String description;

    /**
     * Whether the group is active
     */
    private Boolean active;
}
