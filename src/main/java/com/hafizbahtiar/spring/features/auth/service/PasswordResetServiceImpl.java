package com.hafizbahtiar.spring.features.auth.service;

import com.hafizbahtiar.spring.features.auth.dto.ForgotPasswordRequest;
import com.hafizbahtiar.spring.features.auth.dto.PasswordResetResponse;
import com.hafizbahtiar.spring.features.auth.dto.ResetPasswordRequest;
import com.hafizbahtiar.spring.common.service.EmailService;
import com.hafizbahtiar.spring.features.auth.entity.PasswordResetToken;
import com.hafizbahtiar.spring.features.auth.exception.*;
import com.hafizbahtiar.spring.features.auth.repository.PasswordResetTokenRepository;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service implementation for password reset operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthLoggingService authLoggingService;
    private final EmailService emailService;

    @Value("${app.password-reset.expiration-hours:1}")
    private int expirationHours;

    @Value("${app.password-reset.max-requests-per-hour:3}")
    private int maxRequestsPerHour;

    @Override
    public PasswordResetResponse requestPasswordReset(ForgotPasswordRequest request) {
        log.debug("Password reset requested for email: {}", request.getEmail());
        HttpServletRequest httpRequest = getCurrentRequest();

        try {
            // Find user by email
            User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                    .orElseThrow(() -> {
                        // Don't reveal if user exists (security best practice)
                        log.warn("Password reset requested for non-existent email: {}", request.getEmail());
                        // Log failed request
                        authLoggingService.logPasswordResetRequested(
                                null,
                                request.getEmail(),
                                false,
                                "USER_NOT_FOUND",
                                httpRequest);
                        return PasswordResetException.requestFailed();
                    });

            // Check rate limiting - max requests per hour
            long activeTokenCount = tokenRepository.countByUserIdAndUsedFalse(user.getId());
            if (activeTokenCount >= maxRequestsPerHour) {
                log.warn("Password reset rate limit exceeded for user: {}", user.getEmail());
                authLoggingService.logPasswordResetRequested(
                        user.getId(),
                        user.getEmail(),
                        false,
                        "RATE_LIMIT_EXCEEDED",
                        httpRequest);
                throw PasswordResetException.rateLimitExceeded();
            }

            // Generate expiration time
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

            // Create password reset token
            PasswordResetToken token = new PasswordResetToken(user, expiresAt);
            tokenRepository.save(token);

            log.info("Password reset token generated for user: {}", user.getEmail());

            // Log successful request
            authLoggingService.logPasswordResetRequested(
                    user.getId(),
                    user.getEmail(),
                    true,
                    null,
                    httpRequest);

            // Send email with reset link
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token.getToken(), user.getFullName());
                log.info("Password reset email sent to: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send password reset email to: {}", user.getEmail(), e);
                // Don't fail the request if email fails - token is still created
                // User can request another reset if needed
            }

            // Return success response (don't reveal if user exists)
            return PasswordResetResponse.builder()
                    .success(true)
                    .message("If an account exists with this email, a password reset link has been sent.")
                    .build();

        } catch (PasswordResetException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing password reset request", e);
            authLoggingService.logPasswordResetRequested(
                    null,
                    request.getEmail(),
                    false,
                    "INTERNAL_ERROR",
                    httpRequest);
            throw PasswordResetException.requestFailed();
        }
    }

    @Override
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        log.debug("Password reset attempt with token");
        HttpServletRequest httpRequest = getCurrentRequest();

        try {
            // Find token
            PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> {
                        log.warn("Password reset attempted with invalid token");
                        authLoggingService.logPasswordResetCompleted(
                                null,
                                null,
                                false,
                                "TOKEN_NOT_FOUND",
                                httpRequest);
                        return PasswordResetTokenNotFoundException.byToken();
                    });

            // Validate token
            if (token.isExpired()) {
                log.warn("Password reset attempted with expired token for user: {}", token.getUser().getId());
                authLoggingService.logPasswordResetCompleted(
                        token.getUser().getId(),
                        token.getUser().getEmail(),
                        false,
                        "TOKEN_EXPIRED",
                        httpRequest);
                throw PasswordResetTokenExpiredException.defaultMessage();
            }

            if (token.isUsed()) {
                log.warn("Password reset attempted with used token for user: {}", token.getUser().getId());
                authLoggingService.logPasswordResetCompleted(
                        token.getUser().getId(),
                        token.getUser().getEmail(),
                        false,
                        "TOKEN_ALREADY_USED",
                        httpRequest);
                throw PasswordResetTokenUsedException.defaultMessage();
            }

            // Get user
            User user = token.getUser();

            // Update password
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.setPasswordHash(encodedPassword);
            userRepository.save(user);

            // Mark token as used
            token.markAsUsed();
            tokenRepository.save(token);

            log.info("Password reset completed successfully for user: {}", user.getEmail());

            // Log successful reset
            authLoggingService.logPasswordResetCompleted(
                    user.getId(),
                    user.getEmail(),
                    true,
                    null,
                    httpRequest);

            return PasswordResetResponse.builder()
                    .success(true)
                    .message("Password has been reset successfully")
                    .build();

        } catch (PasswordResetTokenNotFoundException | PasswordResetTokenExpiredException
                | PasswordResetTokenUsedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resetting password", e);
            authLoggingService.logPasswordResetCompleted(
                    null,
                    null,
                    false,
                    "INTERNAL_ERROR",
                    httpRequest);
            throw PasswordResetException.resetFailed();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    /**
     * Get current HTTP request from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
