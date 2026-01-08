package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.AccountDeletionResponse;
import com.hafizbahtiar.spring.features.user.dto.DeleteAccountRequest;
import com.hafizbahtiar.spring.features.user.service.AccountManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for account management endpoints.
 * Handles password changes, account deletion, deactivation, and data export.
 */
@RestController
@RequestMapping("/api/v1/users/account")
@RequiredArgsConstructor
@Slf4j
public class AccountManagementController {

    private final AccountManagementService accountManagementService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Request account deletion - sends confirmation email
     * POST /api/v1/users/account/request-deletion
     * Requires: Authenticated user
     * Returns: AccountDeletionResponse with confirmation message
     */
    @PostMapping("/request-deletion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AccountDeletionResponse>> requestAccountDeletion(
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Account deletion request received for user ID: {}", userId);
        AccountDeletionResponse response = accountManagementService.requestAccountDeletion(userId, httpRequest);
        return ResponseUtils.ok(response);
    }

    /**
     * Delete account using confirmation token
     * DELETE /api/v1/users/account
     * Requires: Authenticated user
     * Body: DeleteAccountRequest (confirmationToken)
     * Returns: Success message
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Account deletion confirmation received for user ID: {}", userId);
        accountManagementService.deleteAccount(userId, request, httpRequest);
        return ResponseUtils.ok(null, "Account deleted successfully");
    }

    /**
     * Deactivate account
     * POST /api/v1/users/account/deactivate
     * Requires: Authenticated user
     * Returns: Success message
     */
    @PostMapping("/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Account deactivation request received for user ID: {}", userId);
        accountManagementService.deactivateAccount(userId, httpRequest);
        return ResponseUtils.ok(null, "Account deactivated successfully");
    }

    /**
     * Reactivate account
     * POST /api/v1/users/account/reactivate
     * Requires: Authenticated user
     * Returns: Success message
     */
    @PostMapping("/reactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reactivateAccount(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Account reactivation request received for user ID: {}", userId);
        accountManagementService.reactivateAccount(userId, httpRequest);
        return ResponseUtils.ok(null, "Account reactivated successfully");
    }

    /**
     * Export user data
     * GET /api/v1/users/account/export?format=json|csv
     * Requires: Authenticated user
     * Returns: File download (JSON or CSV)
     */
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportUserData(
            @RequestParam(defaultValue = "json") String format,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Data export request received for user ID: {}, format: {}", userId, format);

        // Validate format
        if (!"json".equalsIgnoreCase(format) && !"csv".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("Format must be 'json' or 'csv'");
        }

        // Export data
        ByteArrayOutputStream outputStream = accountManagementService.exportUserData(userId, format, httpRequest);
        byte[] data = outputStream.toByteArray();

        // Determine content type and file extension
        String contentType = "json".equalsIgnoreCase(format) ? MediaType.APPLICATION_JSON_VALUE
                : "text/csv";
        String extension = "json".equalsIgnoreCase(format) ? "json" : "csv";
        String filename = "account-data-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "." + extension;

        // Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}

