package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.EmailVerificationRequest;
import com.hafizbahtiar.spring.features.user.dto.EmailVerificationResponse;
import com.hafizbahtiar.spring.features.user.dto.ResendVerificationRequest;
import com.hafizbahtiar.spring.features.user.entity.User;

/**
 * Service interface for email verification operations.
 */
public interface EmailVerificationService {

    /**
     * Generate a verification token for a user.
     *
     * @param user User to generate token for
     * @return The generated token string
     */
    String generateVerificationToken(User user);

    /**
     * Verify email using a token.
     *
     * @param request Email verification request containing token
     * @return Email verification response with user info
     */
    EmailVerificationResponse verifyEmail(EmailVerificationRequest request);

    /**
     * Resend verification email to a user.
     *
     * @param request Resend verification request containing email
     * @return Email verification response
     */
    EmailVerificationResponse resendVerificationEmail(ResendVerificationRequest request);
}
