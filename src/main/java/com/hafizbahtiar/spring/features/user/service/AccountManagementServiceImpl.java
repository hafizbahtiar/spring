package com.hafizbahtiar.spring.features.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hafizbahtiar.spring.common.service.EmailService;
import com.hafizbahtiar.spring.features.auth.service.SessionService;
import com.hafizbahtiar.spring.features.user.dto.AccountDeletionResponse;
import com.hafizbahtiar.spring.features.user.dto.ChangePasswordRequest;
import com.hafizbahtiar.spring.features.user.dto.DeleteAccountRequest;
import com.hafizbahtiar.spring.features.user.entity.AccountDeletionToken;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.AccountDeletionTokenRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AccountManagementService.
 * Handles password changes, account deletion, deactivation, and data export.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountManagementServiceImpl implements AccountManagementService {

    private final UserRepository userRepository;
    private final AccountDeletionTokenRepository deletionTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final UserActivityLoggingService userActivityLoggingService;

    @Value("${app.account-deletion.expiration-days:7}")
    private int deletionExpirationDays;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        log.debug("Password change request for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Password change failed - incorrect current password for user ID: {}", userId);
            userActivityLoggingService.logPasswordChangeFailed(userId, httpRequest);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            log.warn("Password change failed - new password same as current for user ID: {}", userId);
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);

        // Invalidate all sessions (force re-login)
        try {
            // Get all active sessions and revoke them
            var activeSessions = sessionService.getActiveSessions(userId);
            for (var session : activeSessions) {
                sessionService.revokeSession(userId, session.getId());
            }
            log.info("All sessions revoked for user ID: {} after password change", userId);
        } catch (Exception e) {
            log.error("Failed to revoke sessions after password change for user ID: {}", userId, e);
            // Don't fail password change if session revocation fails
        }

        // Send email notification
        try {
            String emailBody = String.format(
                    "Hello %s,\n\n" +
                            "Your password has been successfully changed.\n\n" +
                            "If you did not make this change, please contact support immediately.\n\n" +
                            "Best regards,\n" +
                            "The Team",
                    user.getFullName());
            emailService.sendEmail(user.getEmail(), "Password Changed", emailBody);
            log.info("Password change notification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password change notification email to: {}", user.getEmail(), e);
            // Don't fail password change if email fails
        }

        // Log password change
        userActivityLoggingService.logPasswordChanged(userId, httpRequest);
    }

    @Override
    public AccountDeletionResponse requestAccountDeletion(Long userId, HttpServletRequest httpRequest) {
        log.debug("Account deletion requested for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Check if there's already an active deletion token
        LocalDateTime now = LocalDateTime.now();
        deletionTokenRepository.findActiveTokenByUserId(userId, now)
                .ifPresent(token -> {
                    log.info("Active deletion token already exists for user ID: {}", userId);
                    // Token already exists, don't create a new one
                });

        // Generate expiration time (default 7 days)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(deletionExpirationDays);

        // Create deletion token
        AccountDeletionToken token = new AccountDeletionToken(user, expiresAt);
        deletionTokenRepository.save(token);

        log.info("Account deletion token generated for user: {}", user.getEmail());

        // Send confirmation email
        try {
            String deletionLink = frontendUrl + "/account/delete?token=" + token.getToken();
            String emailBody = String.format(
                    "Hello %s,\n\n" +
                            "You have requested to delete your account.\n\n" +
                            "To confirm the deletion, please click the link below:\n" +
                            "%s\n\n" +
                            "This link will expire in %d days.\n\n" +
                            "If you did not request this, please ignore this email and your account will remain active.\n\n" +
                            "Best regards,\n" +
                            "The Team",
                    user.getFullName(), deletionLink, deletionExpirationDays);
            emailService.sendEmail(user.getEmail(), "Confirm Account Deletion", emailBody);
            log.info("Account deletion confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account deletion confirmation email to: {}", user.getEmail(), e);
            // Don't fail deletion request if email fails
        }

        // Log deletion request
        userActivityLoggingService.logAccountDeletionRequested(userId, httpRequest);

        return AccountDeletionResponse.builder()
                .message("Account deletion confirmation email has been sent")
                .deletionRequested(true)
                .email(user.getEmail())
                .build();
    }

    @Override
    public void deleteAccount(Long userId, DeleteAccountRequest request, HttpServletRequest httpRequest) {
        log.debug("Account deletion confirmation for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Find and validate token
        LocalDateTime now = LocalDateTime.now();
        AccountDeletionToken token = deletionTokenRepository.findActiveTokenByToken(request.getConfirmationToken(), now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired deletion token"));

        // Verify token belongs to user
        if (!token.getUser().getId().equals(userId)) {
            log.warn("Deletion token does not belong to user ID: {}", userId);
            throw new SecurityException("Invalid deletion token");
        }

        // Mark token as used
        token.markAsUsed();
        deletionTokenRepository.save(token);

        // Soft delete user (set active = false)
        user.deactivate();
        userRepository.save(user);

        // Revoke all sessions
        try {
            var activeSessions = sessionService.getActiveSessions(userId);
            for (var session : activeSessions) {
                sessionService.revokeSession(userId, session.getId());
            }
            log.info("All sessions revoked for user ID: {} after account deletion", userId);
        } catch (Exception e) {
            log.error("Failed to revoke sessions after account deletion for user ID: {}", userId, e);
            // Continue with deletion even if session revocation fails
        }

        log.info("Account deleted (soft delete) for user ID: {}", userId);

        // Send confirmation email
        try {
            String emailBody = String.format(
                    "Hello %s,\n\n" +
                            "Your account has been successfully deleted.\n\n" +
                            "All your data has been removed from our system.\n\n" +
                            "If you did not request this deletion, please contact support immediately.\n\n" +
                            "Best regards,\n" +
                            "The Team",
                    user.getFullName());
            emailService.sendEmail(user.getEmail(), "Account Deleted", emailBody);
            log.info("Account deletion confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account deletion confirmation email to: {}", user.getEmail(), e);
            // Don't fail deletion if email fails
        }

        // Log account deletion
        userActivityLoggingService.logAccountDeleted(userId, httpRequest);
    }

    @Override
    public void deactivateAccount(Long userId, HttpServletRequest httpRequest) {
        log.debug("Account deactivation request for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        if (!user.isActive()) {
            log.info("Account already deactivated for user ID: {}", userId);
            return; // Idempotent operation
        }

        // Deactivate user
        user.deactivate();
        userRepository.save(user);

        // Revoke all sessions
        try {
            var activeSessions = sessionService.getActiveSessions(userId);
            for (var session : activeSessions) {
                sessionService.revokeSession(userId, session.getId());
            }
            log.info("All sessions revoked for user ID: {} after account deactivation", userId);
        } catch (Exception e) {
            log.error("Failed to revoke sessions after account deactivation for user ID: {}", userId, e);
            // Continue with deactivation even if session revocation fails
        }

        log.info("Account deactivated for user ID: {}", userId);

        // Send email notification
        try {
            String emailBody = String.format(
                    "Hello %s,\n\n" +
                            "Your account has been deactivated.\n\n" +
                            "You can reactivate your account at any time by logging in.\n\n" +
                            "If you did not request this, please contact support immediately.\n\n" +
                            "Best regards,\n" +
                            "The Team",
                    user.getFullName());
            emailService.sendEmail(user.getEmail(), "Account Deactivated", emailBody);
            log.info("Account deactivation notification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account deactivation notification email to: {}", user.getEmail(), e);
            // Don't fail deactivation if email fails
        }

        // Log account deactivation
        userActivityLoggingService.logAccountDeactivated(userId, httpRequest);
    }

    @Override
    public void reactivateAccount(Long userId, HttpServletRequest httpRequest) {
        log.debug("Account reactivation request for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        if (user.isActive()) {
            log.info("Account already active for user ID: {}", userId);
            return; // Idempotent operation
        }

        // Reactivate user
        user.activate();
        userRepository.save(user);

        log.info("Account reactivated for user ID: {}", userId);

        // Send email notification
        try {
            String emailBody = String.format(
                    "Hello %s,\n\n" +
                            "Your account has been reactivated.\n\n" +
                            "You can now log in and access all features.\n\n" +
                            "Best regards,\n" +
                            "The Team",
                    user.getFullName());
            emailService.sendEmail(user.getEmail(), "Account Reactivated", emailBody);
            log.info("Account reactivation notification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account reactivation notification email to: {}", user.getEmail(), e);
            // Don't fail reactivation if email fails
        }

        // Log account reactivation
        userActivityLoggingService.logAccountReactivated(userId, httpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream exportUserData(Long userId, String format, HttpServletRequest httpRequest) {
        log.debug("Data export request for user ID: {}, format: {}", userId, format);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Build data map with all user information
        Map<String, Object> userData = new HashMap<>();
        userData.put("exportedAt", LocalDateTime.now().toString());
        userData.put("userId", user.getId());
        userData.put("email", user.getEmail());
        userData.put("username", user.getUsername());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole());
        userData.put("emailVerified", user.isEmailVerified());
        userData.put("active", user.isActive());
        userData.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        userData.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        userData.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);

        // Note: For a complete GDPR export, you would also include:
        // - Portfolio data (skills, experiences, projects, etc.)
        // - Preferences (user, notification, currency)
        // - Sessions
        // - Activity logs
        // - Payment/subscription data (if applicable)
        // This is a simplified version focusing on core user data

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            if ("json".equalsIgnoreCase(format)) {
                // Export as JSON - create ObjectMapper locally
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, userData);
            } else if ("csv".equalsIgnoreCase(format)) {
                // Export as CSV
                exportToCsv(userData, outputStream);
            } else {
                throw new IllegalArgumentException("Unsupported export format: " + format);
            }

            log.info("Data exported successfully for user ID: {}, format: {}", userId, format);

            // Log data export
            userActivityLoggingService.logDataExported(userId, format, httpRequest);

            return outputStream;
        } catch (IOException e) {
            log.error("Failed to export data for user ID: {}", userId, e);
            throw new RuntimeException("Failed to export user data", e);
        }
    }

    /**
     * Export user data to CSV format
     */
    private void exportToCsv(Map<String, Object> userData, ByteArrayOutputStream outputStream) throws IOException {
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // Write header
        writer.println("Field,Value");

        // Write data rows
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            String key = escapeCsvValue(entry.getKey());
            String value = entry.getValue() != null ? escapeCsvValue(entry.getValue().toString()) : "";
            writer.println(key + "," + value);
        }

        writer.flush();
    }

    /**
     * Escape CSV value (handle commas, quotes, newlines)
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

}

