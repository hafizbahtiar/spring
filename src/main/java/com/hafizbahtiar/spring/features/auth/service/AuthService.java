package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.LoginRequest;
import com.hafizbahtiar.spring.features.auth.dto.LoginResponse;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;

public interface AuthService {

    /**
     * Authenticate user with credentials and return JWT token
     *
     * @param request Login request with identifier (email/username) and password
     * @return LoginResponse with JWT token and user information
     */
    LoginResponse login(LoginRequest request);

    /**
     * Get current authenticated user
     *
     * @return UserResponse of current user
     */
    UserResponse getCurrentUser();

    /**
     * Validate JWT token
     *
     * @param token JWT token to validate
     * @return true if token is valid
     */
    boolean validateToken(String token);

    /**
     * Logout current user - revokes the current session
     *
     * @return void
     */
    void logout();

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken Refresh token (UUID from sessionId)
     * @param request      HTTP request for logging
     * @return TokenRefreshResponse with new access token
     */
    com.hafizbahtiar.spring.features.auth.dto.TokenRefreshResponse refreshToken(String refreshToken,
            jakarta.servlet.http.HttpServletRequest request);
}
