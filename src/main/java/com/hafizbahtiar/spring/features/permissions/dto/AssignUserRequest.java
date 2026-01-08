package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning a user to a permission group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRequest {

    /**
     * User ID to assign to the group
     */
    @NotNull(message = "User ID is required")
    private Long userId;
}

