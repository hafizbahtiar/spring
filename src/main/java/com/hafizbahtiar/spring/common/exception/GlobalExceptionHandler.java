package com.hafizbahtiar.spring.common.exception;

import com.hafizbahtiar.spring.common.dto.ApiErrorResponse;
import com.hafizbahtiar.spring.features.auth.exception.InvalidCredentialsException;
import com.hafizbahtiar.spring.features.auth.exception.PasswordResetException;
import com.hafizbahtiar.spring.features.auth.exception.PasswordResetTokenExpiredException;
import com.hafizbahtiar.spring.features.auth.exception.PasswordResetTokenNotFoundException;
import com.hafizbahtiar.spring.features.auth.exception.PasswordResetTokenUsedException;
import com.hafizbahtiar.spring.features.auth.exception.SessionNotFoundException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentMethodException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentMethodNotFoundException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentNotFoundException;
import com.hafizbahtiar.spring.features.payment.exception.PaymentProcessingException;
import com.hafizbahtiar.spring.features.payment.exception.RefundException;
import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionCancellationException;
import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionException;
import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionNotFoundException;
import com.hafizbahtiar.spring.features.subscription.exception.SubscriptionPlanNotFoundException;
import com.hafizbahtiar.spring.features.user.exception.RoleException;
import com.hafizbahtiar.spring.features.user.exception.UserAlreadyExistsException;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.CertificationNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.CompanyNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.TestimonialNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.EducationNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.ExperienceNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
import com.hafizbahtiar.spring.features.portfolio.exception.ProjectNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.SkillNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
                        UserNotFoundException ex, HttpServletRequest request) {
                log.debug("User not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "USER_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(
                        UserAlreadyExistsException ex, HttpServletRequest request) {
                log.debug("User already exists: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "USER_ALREADY_EXISTS",
                                ex.getMessage(),
                                HttpStatus.CONFLICT.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(RoleException.class)
        public ResponseEntity<ApiErrorResponse> handleRoleException(
                        RoleException ex, HttpServletRequest request) {
                log.debug("Role validation error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "ROLE_VALIDATION_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI(),
                                ex.getValidationErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(
                        InvalidCredentialsException ex, HttpServletRequest request) {
                log.debug("Invalid credentials: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "INVALID_CREDENTIALS",
                                ex.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, HttpServletRequest request) {
                log.debug("Bad credentials: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "INVALID_CREDENTIALS",
                                "Invalid credentials",
                                HttpStatus.UNAUTHORIZED.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(PasswordResetTokenNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handlePasswordResetTokenNotFoundException(
                        PasswordResetTokenNotFoundException ex, HttpServletRequest request) {
                log.debug("Password reset token not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PASSWORD_RESET_TOKEN_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(PasswordResetTokenExpiredException.class)
        public ResponseEntity<ApiErrorResponse> handlePasswordResetTokenExpiredException(
                        PasswordResetTokenExpiredException ex, HttpServletRequest request) {
                log.debug("Password reset token expired: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PASSWORD_RESET_TOKEN_EXPIRED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(PasswordResetTokenUsedException.class)
        public ResponseEntity<ApiErrorResponse> handlePasswordResetTokenUsedException(
                        PasswordResetTokenUsedException ex, HttpServletRequest request) {
                log.debug("Password reset token already used: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PASSWORD_RESET_TOKEN_USED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(PasswordResetException.class)
        public ResponseEntity<ApiErrorResponse> handlePasswordResetException(
                        PasswordResetException ex, HttpServletRequest request) {
                log.debug("Password reset error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PASSWORD_RESET_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(SessionNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleSessionNotFoundException(
                        SessionNotFoundException ex, HttpServletRequest request) {
                log.debug("Session not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "SESSION_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // Email Verification Exceptions
        @ExceptionHandler(com.hafizbahtiar.spring.features.user.exception.EmailVerificationTokenNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleEmailVerificationTokenNotFoundException(
                        com.hafizbahtiar.spring.features.user.exception.EmailVerificationTokenNotFoundException ex,
                        HttpServletRequest request) {
                log.debug("Email verification token not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "EMAIL_VERIFICATION_TOKEN_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(com.hafizbahtiar.spring.features.user.exception.EmailVerificationTokenExpiredException.class)
        public ResponseEntity<ApiErrorResponse> handleEmailVerificationTokenExpiredException(
                        com.hafizbahtiar.spring.features.user.exception.EmailVerificationTokenExpiredException ex,
                        HttpServletRequest request) {
                log.debug("Email verification token expired: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "EMAIL_VERIFICATION_TOKEN_EXPIRED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(com.hafizbahtiar.spring.features.user.exception.EmailVerificationTokenUsedException.class)
        public ResponseEntity<ApiErrorResponse> handleEmailVerificationTokenUsedException(
                        com.hafizbahtiar.spring.features.user.exception.EmailVerificationTokenUsedException ex,
                        HttpServletRequest request) {
                log.debug("Email verification token used: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "EMAIL_VERIFICATION_TOKEN_USED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(com.hafizbahtiar.spring.features.user.exception.EmailVerificationException.class)
        public ResponseEntity<ApiErrorResponse> handleEmailVerificationException(
                        com.hafizbahtiar.spring.features.user.exception.EmailVerificationException ex,
                        HttpServletRequest request) {
                log.debug("Email verification error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "EMAIL_VERIFICATION_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(
                        ValidationException ex, HttpServletRequest request) {
                log.debug("Validation error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "VALIDATION_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI(),
                                ex.getValidationErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                log.debug("Validation error: {}", ex.getMessage());

                Map<String, String> validationErrors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        validationErrors.put(fieldName, errorMessage);
                });

                ApiErrorResponse error = ApiErrorResponse.of(
                                "VALIDATION_ERROR",
                                "Validation failed",
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI(),
                                validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {
                log.debug("Illegal argument: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "ILLEGAL_ARGUMENT",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Payment-related exceptions

        @ExceptionHandler(PaymentNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handlePaymentNotFoundException(
                        PaymentNotFoundException ex, HttpServletRequest request) {
                log.debug("Payment not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PAYMENT_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(PaymentProcessingException.class)
        public ResponseEntity<ApiErrorResponse> handlePaymentProcessingException(
                        PaymentProcessingException ex, HttpServletRequest request) {
                log.warn("Payment processing failed: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PAYMENT_PROCESSING_FAILED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(RefundException.class)
        public ResponseEntity<ApiErrorResponse> handleRefundException(
                        RefundException ex, HttpServletRequest request) {
                log.warn("Refund processing failed: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "REFUND_FAILED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(PaymentMethodNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handlePaymentMethodNotFoundException(
                        PaymentMethodNotFoundException ex, HttpServletRequest request) {
                log.debug("Payment method not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PAYMENT_METHOD_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(PaymentMethodException.class)
        public ResponseEntity<ApiErrorResponse> handlePaymentMethodException(
                        PaymentMethodException ex, HttpServletRequest request) {
                log.warn("Payment method error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PAYMENT_METHOD_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(ProviderException.class)
        public ResponseEntity<ApiErrorResponse> handleProviderException(
                        ProviderException ex, HttpServletRequest request) {
                log.error("Provider error [{}]: {}", ex.getProvider(), ex.getMessage());

                // Build error message with provider context
                String errorMessage = ex.getProvider() != null
                                ? String.format("[%s] %s", ex.getProvider(), ex.getMessage())
                                : ex.getMessage();

                // Include provider error code in metadata if available
                Map<String, String> metadata = null;
                if (ex.getProviderErrorCode() != null || ex.getProvider() != null) {
                        metadata = new HashMap<>();
                        if (ex.getProvider() != null) {
                                metadata.put("provider", ex.getProvider());
                        }
                        if (ex.getProviderErrorCode() != null) {
                                metadata.put("providerErrorCode", ex.getProviderErrorCode());
                        }
                }

                ApiErrorResponse error = metadata != null
                                ? ApiErrorResponse.of(
                                                "PROVIDER_ERROR",
                                                errorMessage,
                                                HttpStatus.BAD_GATEWAY.value(), // 502 Bad Gateway for external provider
                                                                                // errors
                                                request.getRequestURI(),
                                                metadata)
                                : ApiErrorResponse.of(
                                                "PROVIDER_ERROR",
                                                errorMessage,
                                                HttpStatus.BAD_GATEWAY.value(),
                                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }

        // Subscription-related exceptions

        @ExceptionHandler(SubscriptionNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleSubscriptionNotFoundException(
                        SubscriptionNotFoundException ex, HttpServletRequest request) {
                log.debug("Subscription not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "SUBSCRIPTION_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(SubscriptionPlanNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleSubscriptionPlanNotFoundException(
                        SubscriptionPlanNotFoundException ex, HttpServletRequest request) {
                log.debug("Subscription plan not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "SUBSCRIPTION_PLAN_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(SubscriptionException.class)
        public ResponseEntity<ApiErrorResponse> handleSubscriptionException(
                        SubscriptionException ex, HttpServletRequest request) {
                log.warn("Subscription error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "SUBSCRIPTION_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(SubscriptionCancellationException.class)
        public ResponseEntity<ApiErrorResponse> handleSubscriptionCancellationException(
                        SubscriptionCancellationException ex, HttpServletRequest request) {
                log.warn("Subscription cancellation error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "SUBSCRIPTION_CANCELLATION_FAILED",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Portfolio-related exceptions

        @ExceptionHandler(SkillNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleSkillNotFoundException(
                        SkillNotFoundException ex, HttpServletRequest request) {
                log.debug("Skill not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "SKILL_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(ExperienceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleExperienceNotFoundException(
                        ExperienceNotFoundException ex, HttpServletRequest request) {
                log.debug("Experience not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "EXPERIENCE_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(ProjectNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleProjectNotFoundException(
                        ProjectNotFoundException ex, HttpServletRequest request) {
                log.debug("Project not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PROJECT_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(EducationNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleEducationNotFoundException(
                        EducationNotFoundException ex, HttpServletRequest request) {
                log.debug("Education not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "EDUCATION_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(CompanyNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleCompanyNotFoundException(
                        CompanyNotFoundException ex, HttpServletRequest request) {
                log.debug("Company not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "COMPANY_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(CertificationNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleCertificationNotFoundException(
                        CertificationNotFoundException ex, HttpServletRequest request) {
                log.debug("Certification not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "CERTIFICATION_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(TestimonialNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleTestimonialNotFoundException(
                        TestimonialNotFoundException ex, HttpServletRequest request) {
                log.debug("Testimonial not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "TESTIMONIAL_NOT_FOUND",
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(PortfolioException.class)
        public ResponseEntity<ApiErrorResponse> handlePortfolioException(
                        PortfolioException ex, HttpServletRequest request) {
                log.debug("Portfolio error: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "PORTFOLIO_ERROR",
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(com.hafizbahtiar.spring.features.portfolio.exception.ContactNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleContactNotFoundException(
                        com.hafizbahtiar.spring.features.portfolio.exception.ContactNotFoundException ex,
                        HttpServletRequest request) {
                log.debug("Contact not found: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of("CONTACT_NOT_FOUND", ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(), request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceededException(
                        MaxUploadSizeExceededException ex, HttpServletRequest request) {
                log.warn("File upload size exceeded: {}", ex.getMessage());
                ApiErrorResponse error = ApiErrorResponse.of(
                                "FILE_TOO_LARGE",
                                "File size exceeds the maximum allowed size. Please upload a file smaller than 10MB.",
                                413, // HTTP 413 Payload Too Large
                                request.getRequestURI());
                return ResponseEntity.status(413).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {
                log.error("Unexpected error occurred", ex);
                ApiErrorResponse error = ApiErrorResponse.of(
                                "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
