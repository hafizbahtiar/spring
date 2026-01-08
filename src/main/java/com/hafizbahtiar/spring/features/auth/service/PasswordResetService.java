package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.ForgotPasswordRequest;
import com.hafizbahtiar.spring.features.auth.dto.PasswordResetResponse;
import com.hafizbahtiar.spring.features.auth.dto.ResetPasswordRequest;

/**
 * Service interface for password reset operations.
 */
public interface PasswordResetService {

    /**
     * Request password reset - generates token and sends email
     *
     * @param request Forgot password request with email
     * @return PasswordResetResponse with success message
     */
    PasswordResetResponse requestPasswordReset(ForgotPasswordRequest request);

    /**
     * Reset password using token
     *
     * @param request Reset password request with token and new password
     * @return PasswordResetResponse with success message
     */
    PasswordResetResponse resetPassword(ResetPasswordRequest request);

    /**
     * Validate password reset token
     *
     * @param token Token to validate
     * @return true if token is valid (not expired and not used)
     */
    boolean validateToken(String token);
}
