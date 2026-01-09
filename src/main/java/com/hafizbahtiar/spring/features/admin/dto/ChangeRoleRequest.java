package com.hafizbahtiar.spring.features.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for changing a user's role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ADMIN|OWNER)$", message = "Role must be USER, ADMIN, or OWNER")
    private String role;
}
