package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.AccountDeletionResponse;
import com.hafizbahtiar.spring.features.user.dto.ChangePasswordRequest;
import com.hafizbahtiar.spring.features.user.dto.DeleteAccountRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;

/**
 * Service interface for account management operations.
 * Handles password changes, account deletion, deactivation, and data export.
 */
public interface AccountManagementService {

    /**
     * Change user password.
     *
     * @param userId  User ID
     * @param request Change password request with current and new password
     * @param request HTTP request for logging
     */
    void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest);

    /**
     * Request account deletion - generates token and sends confirmation email.
     *
     * @param userId      User ID
     * @param httpRequest HTTP request for logging
     * @return AccountDeletionResponse with confirmation message
     */
    AccountDeletionResponse requestAccountDeletion(Long userId, HttpServletRequest httpRequest);

    /**
     * Delete account using confirmation token.
     *
     * @param userId  User ID
     * @param request Delete account request with confirmation token
     * @param httpRequest HTTP request for logging
     */
    void deleteAccount(Long userId, DeleteAccountRequest request, HttpServletRequest httpRequest);

    /**
     * Deactivate user account.
     *
     * @param userId      User ID
     * @param httpRequest HTTP request for logging
     */
    void deactivateAccount(Long userId, HttpServletRequest httpRequest);

    /**
     * Reactivate user account.
     *
     * @param userId      User ID
     * @param httpRequest HTTP request for logging
     */
    void reactivateAccount(Long userId, HttpServletRequest httpRequest);

    /**
     * Export user data in specified format.
     *
     * @param userId User ID
     * @param format Export format (json or csv)
     * @param httpRequest HTTP request for logging
     * @return ByteArrayOutputStream containing exported data
     */
    ByteArrayOutputStream exportUserData(Long userId, String format, HttpServletRequest httpRequest);
}

