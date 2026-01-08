package com.hafizbahtiar.spring.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for token refresh endpoint.
 * Returns new access token and expiration information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {

    /**
     * New JWT access token (15 minutes expiration)
     */
    private String token;

    /**
     * Token type (always "Bearer")
     */
    @Builder.Default
    private String type = "Bearer";

    /**
     * Access token expiration timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * Refresh token (same as provided, unless implementing token rotation)
     * Optional: Only included if implementing refresh token rotation
     */
    private String refreshToken;

    /**
     * Refresh token expiration timestamp
     * Optional: Only included if implementing refresh token rotation
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refreshTokenExpiresAt;
}

