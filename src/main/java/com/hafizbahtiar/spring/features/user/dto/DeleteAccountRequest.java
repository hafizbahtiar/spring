package com.hafizbahtiar.spring.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for deleting user account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    /**
     * Confirmation token received via email
     */
    @NotBlank(message = "Confirmation token is required")
    private String confirmationToken;
}
