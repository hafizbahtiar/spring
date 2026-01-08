package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.common.service.EmailService;
import com.hafizbahtiar.spring.features.user.dto.EmailVerificationRequest;
import com.hafizbahtiar.spring.features.user.dto.EmailVerificationResponse;
import com.hafizbahtiar.spring.features.user.dto.ResendVerificationRequest;
import com.hafizbahtiar.spring.features.user.entity.EmailVerificationToken;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.*;
import com.hafizbahtiar.spring.features.user.repository.EmailVerificationTokenRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service implementation for email verification operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

        private final UserRepository userRepository;
        private final EmailVerificationTokenRepository tokenRepository;
        private final EmailService emailService;
        private final UserActivityLoggingService userActivityLoggingService;

        @Value("${app.email-verification.expiration-hours:24}")
        private int expirationHours;

        @Value("${app.email-verification.max-requests-per-hour:3}")
        private int maxRequestsPerHour;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateVerificationToken(User user) {
        try {
            log.debug("Generating email verification token for user: {}", user.getEmail());

            // Invalidate any existing unverified tokens for this user
            tokenRepository.findByUserIdAndVerifiedFalse(user.getId())
                    .forEach(token -> {
                        // Mark as verified to prevent further use (or delete them)
                        tokenRepository.delete(token);
                    });

            // Generate expiration time
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

            // Create email verification token
            EmailVerificationToken token = new EmailVerificationToken(user, expiresAt);
            tokenRepository.save(token);

            log.info("Email verification token generated for user: {}", user.getEmail());
            return token.getToken();
        } catch (Exception e) {
            log.error("Failed to generate email verification token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate email verification token", e);
        }
    }

        @Override
        public EmailVerificationResponse verifyEmail(EmailVerificationRequest request) {
                log.debug("Email verification attempt with token");
                HttpServletRequest httpRequest = getCurrentRequest();

                EmailVerificationToken token = tokenRepository.findByToken(request.getToken())
                                .orElseThrow(() -> {
                                        userActivityLoggingService.logEmailVerificationFailed(
                                                        null,
                                                        null,
                                                        "TOKEN_NOT_FOUND",
                                                        httpRequest);
                                        return EmailVerificationTokenNotFoundException.byToken(request.getToken());
                                });

                // Check if token is expired
                if (token.isExpired()) {
                        userActivityLoggingService.logEmailVerificationFailed(
                                        token.getUser().getId(),
                                        token.getUser().getEmail(),
                                        "TOKEN_EXPIRED",
                                        httpRequest);
                        throw EmailVerificationTokenExpiredException.defaultMessage();
                }

                // Check if token is already verified
                if (token.isVerified()) {
                        userActivityLoggingService.logEmailVerificationFailed(
                                        token.getUser().getId(),
                                        token.getUser().getEmail(),
                                        "TOKEN_ALREADY_USED",
                                        httpRequest);
                        throw EmailVerificationTokenUsedException.defaultMessage();
                }

                User user = token.getUser();

                // Check if email is already verified
                if (user.isEmailVerified()) {
                        userActivityLoggingService.logEmailVerificationFailed(
                                        user.getId(),
                                        user.getEmail(),
                                        "EMAIL_ALREADY_VERIFIED",
                                        httpRequest);
                        throw EmailVerificationException.emailAlreadyVerified();
                }

                // Mark token as verified
                token.markAsVerified();
                tokenRepository.save(token);

                // Verify user's email
                user.verifyEmail();
                userRepository.save(user);

                log.info("Email verified successfully for user: {}", user.getEmail());

                // Log successful verification
                userActivityLoggingService.logEmailVerified(
                                user.getId(),
                                user.getEmail(),
                                httpRequest);

                // Build response
                return EmailVerificationResponse.builder()
                                .success(true)
                                .message("Email verified successfully.")
                                .user(EmailVerificationResponse.UserInfo.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .username(user.getUsername())
                                                .fullName(user.getFullName())
                                                .emailVerified(user.isEmailVerified())
                                                .build())
                                .build();
        }

        @Override
        public EmailVerificationResponse resendVerificationEmail(ResendVerificationRequest request) {
                log.debug("Resend verification email requested for: {}", request.getEmail());
                HttpServletRequest httpRequest = getCurrentRequest();

                User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                                .orElseThrow(() -> {
                                        // Don't reveal if user exists (security best practice)
                                        log.warn("Resend verification requested for non-existent email: {}",
                                                        request.getEmail());
                                        userActivityLoggingService.logEmailVerificationSent(
                                                        null,
                                                        request.getEmail(),
                                                        false,
                                                        "USER_NOT_FOUND",
                                                        httpRequest);
                                        // Return success message anyway to prevent user enumeration
                                        return UserNotFoundException.byEmail(request.getEmail());
                                });

                // Check if email is already verified
                if (user.isEmailVerified()) {
                        log.info("Resend verification requested for already verified email: {}", user.getEmail());
                        userActivityLoggingService.logEmailVerificationSent(
                                        user.getId(),
                                        user.getEmail(),
                                        false,
                                        "EMAIL_ALREADY_VERIFIED",
                                        httpRequest);
                        throw EmailVerificationException.emailAlreadyVerified();
                }

                // Check rate limiting - count tokens created in last hour
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                long recentTokens = tokenRepository.findByUserIdAndVerifiedFalse(user.getId()).stream()
                                .filter(t -> t.getCreatedAt().isAfter(oneHourAgo))
                                .count();

                if (recentTokens >= maxRequestsPerHour) {
                        log.warn("Email verification rate limit exceeded for user: {}", user.getEmail());
                        userActivityLoggingService.logEmailVerificationSent(
                                        user.getId(),
                                        user.getEmail(),
                                        false,
                                        "RATE_LIMIT_EXCEEDED",
                                        httpRequest);
                        throw EmailVerificationException.rateLimitExceeded(
                                        "Too many verification email requests. Please try again later.");
                }

                // Generate new token
                String token = generateVerificationToken(user);

                // Send verification email
                try {
                        emailService.sendEmailVerificationEmail(user.getEmail(), token, user.getFullName());
                        log.info("Verification email sent to: {}", user.getEmail());

                        // Log successful email sending
                        userActivityLoggingService.logEmailVerificationSent(
                                        user.getId(),
                                        user.getEmail(),
                                        true,
                                        null,
                                        httpRequest);
                } catch (Exception e) {
                        log.error("Failed to send verification email to: {}", user.getEmail(), e);
                        // Don't fail the request if email fails - token is still created
                        // User can request another verification email if needed
                        userActivityLoggingService.logEmailVerificationSent(
                                        user.getId(),
                                        user.getEmail(),
                                        false,
                                        "EMAIL_SEND_FAILED",
                                        httpRequest);
                }

                return EmailVerificationResponse.builder()
                                .success(true)
                                .message("If an account exists with this email, a verification email has been sent.")
                                .build();
        }

        /**
         * Get current HTTP request from RequestContextHolder
         */
        private HttpServletRequest getCurrentRequest() {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                                .getRequestAttributes();
                return attributes != null ? attributes.getRequest() : null;
        }
}
