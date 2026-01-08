package com.hafizbahtiar.spring.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for refresh token endpoint.
 * Used to obtain a new access token using a refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * Refresh token (UUID from sessionId)
     * Must not be blank
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}

