package com.hafizbahtiar.spring.features.auth.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.auth.dto.ForgotPasswordRequest;
import com.hafizbahtiar.spring.features.auth.dto.LoginRequest;
import com.hafizbahtiar.spring.features.auth.dto.LoginResponse;
import com.hafizbahtiar.spring.features.auth.dto.PasswordResetResponse;
import com.hafizbahtiar.spring.features.auth.dto.RefreshTokenRequest;
import com.hafizbahtiar.spring.features.auth.dto.ResetPasswordRequest;
import com.hafizbahtiar.spring.features.auth.dto.AuthLogResponse;
import com.hafizbahtiar.spring.features.auth.dto.TokenRefreshResponse;
import com.hafizbahtiar.spring.features.auth.service.AuthLogService;
import com.hafizbahtiar.spring.features.auth.service.AuthService;
import com.hafizbahtiar.spring.features.auth.service.PasswordResetService;
import com.hafizbahtiar.spring.features.user.dto.ChangePasswordRequest;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.service.AccountManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final AccountManagementService accountManagementService;
    private final AuthLogService authLogService;

    /**
     * Authenticate user and return JWT token
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for identifier: {}", request.getIdentifier());
        LoginResponse response = authService.login(request);
        return ResponseUtils.ok(response, "Login successful");
    }

    /**
     * Get current authenticated user
     * GET /api/v1/auth/me
     * Requires: Authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.debug("Get current user request");
        UserResponse user = authService.getCurrentUser();
        return ResponseUtils.ok(user);
    }

    /**
     * Validate JWT token
     * POST /api/v1/auth/validate
     * Requires: Authenticated user
     */
    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        log.debug("Token validation request");
        boolean isValid = authService.validateToken(token);
        return ResponseUtils.ok(isValid);
    }

    /**
     * Refresh access token using refresh token
     * POST /api/v1/auth/refresh
     * Public endpoint - no authentication required (uses refresh token for
     * validation)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("Token refresh request received");
        TokenRefreshResponse response = authService.refreshToken(request.getRefreshToken(), httpRequest);
        return ResponseUtils.ok(response, "Token refreshed successfully");
    }

    /**
     * Request password reset - sends reset link to email
     * POST /api/v1/auth/forgot-password
     * Public endpoint - no authentication required
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());
        PasswordResetResponse response = passwordResetService.requestPasswordReset(request);
        return ResponseUtils.ok(response);
    }

    /**
     * Reset password using token
     * POST /api/v1/auth/reset-password
     * Public endpoint - no authentication required
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt with token");
        PasswordResetResponse response = passwordResetService.resetPassword(request);
        return ResponseUtils.ok(response, "Password has been reset successfully");
    }

    /**
     * Logout current user - revokes the current session
     * POST /api/v1/auth/logout
     * Requires: Authenticated user
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request received");
        authService.logout();
        return ResponseUtils.ok(null, "Logout successful");
    }

    /**
     * Change user password
     * POST /api/v1/auth/change-password
     * Requires: Authenticated user
     * Body: ChangePasswordRequest (currentPassword, newPassword)
     * Returns: Success message
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("Password change request received");
        Long userId = getCurrentUserId();
        accountManagementService.changePassword(userId, request, httpRequest);
        return ResponseUtils.ok(null, "Password changed successfully. All sessions have been revoked.");
    }

    /**
     * Get current user's authentication logs
     * GET /api/v1/auth/logs?limit=10
     * Requires: Authenticated user
     * Returns: List of authentication logs for current user
     */
    @GetMapping("/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AuthLogResponse>>> getUserAuthLogs(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Get auth logs request received, limit: {}", limit);
        Long userId = getCurrentUserId();
        List<AuthLogResponse> logs = authLogService.getUserAuthLogs(userId, limit);
        return ResponseUtils.ok(logs);
    }

    /**
     * Get security events (failed logins, token invalidations, etc.)
     * GET /api/v1/auth/logs/security?limit=20
     * Requires: OWNER or ADMIN role
     * Returns: List of security event logs
     */
    @GetMapping("/logs/security")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthLogResponse>>> getSecurityLogs(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Get security logs request received, limit: {}", limit);
        List<AuthLogResponse> logs = authLogService.getSecurityLogs(limit);
        return ResponseUtils.ok(logs);
    }

    /**
     * Get failed login attempts for a specific identifier
     * GET /api/v1/auth/logs/failed-attempts?identifier={email}&limit=10
     * Requires: OWNER or ADMIN role
     * Returns: List of failed login attempt logs
     */
    @GetMapping("/logs/failed-attempts")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthLogResponse>>> getFailedLoginAttempts(
            @RequestParam String identifier,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Get failed login attempts request received for identifier: {}, limit: {}", identifier, limit);
        List<AuthLogResponse> logs = authLogService.getFailedLoginAttempts(identifier, limit);
        return ResponseUtils.ok(logs);
    }

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null
                && authentication
                        .getPrincipal() instanceof com.hafizbahtiar.spring.common.security.UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }
}
