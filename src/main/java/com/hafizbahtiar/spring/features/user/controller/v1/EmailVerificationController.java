package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.EmailVerificationRequest;
import com.hafizbahtiar.spring.features.user.dto.EmailVerificationResponse;
import com.hafizbahtiar.spring.features.user.dto.ResendVerificationRequest;
import com.hafizbahtiar.spring.features.user.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for email verification endpoints.
 * These endpoints are public (no authentication required) to allow users
 * to verify their email addresses.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * Verify email using a token
     * POST /api/v1/auth/verify-email
     * Public endpoint - no authentication required
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        log.info("Email verification request received");
        EmailVerificationResponse response = emailVerificationService.verifyEmail(request);
        return ResponseUtils.ok(response, response.getMessage());
    }

    /**
     * Resend verification email
     * POST /api/v1/auth/resend-verification
     * Public endpoint - no authentication required
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification email requested for: {}", request.getEmail());
        EmailVerificationResponse response = emailVerificationService.resendVerificationEmail(request);
        return ResponseUtils.ok(response, response.getMessage());
    }
}

