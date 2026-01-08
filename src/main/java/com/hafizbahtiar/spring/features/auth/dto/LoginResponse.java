package com.hafizbahtiar.spring.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    /**
     * Refresh token (UUID from sessionId)
     * Used for obtaining new access tokens without re-login
     */
    private String refreshToken;

    /**
     * Refresh token expiration timestamp (7 days from creation)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime refreshTokenExpiresAt;

    private UserResponse user;
}
