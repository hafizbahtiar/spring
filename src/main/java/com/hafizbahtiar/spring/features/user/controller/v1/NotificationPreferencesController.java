package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesResponse;
import com.hafizbahtiar.spring.features.user.service.NotificationPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification preferences management endpoints.
 * Handles notification preferences CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferencesController {

    private final NotificationPreferencesService notificationPreferencesService;

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
     * Get current user's notification preferences
     * GET /api/v1/notifications/preferences
     * Requires: Authenticated user
     * Returns: NotificationPreferencesResponse (creates default preferences if
     * doesn't exist)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getNotificationPreferences() {
        Long userId = getCurrentUserId();
        log.info("Notification preferences fetch request received for user ID: {}", userId);
        NotificationPreferencesResponse response = notificationPreferencesService.getNotificationPreferences(userId);
        return ResponseUtils.ok(response);
    }

    /**
     * Update current user's notification preferences
     * PUT /api/v1/notifications/preferences
     * Requires: Authenticated user
     * Body: NotificationPreferencesRequest (all fields optional for partial
     * updates)
     * Returns: Updated NotificationPreferencesResponse
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferencesRequest request) {
        Long userId = getCurrentUserId();
        log.info("Notification preferences update request received for user ID: {}", userId);
        NotificationPreferencesResponse response = notificationPreferencesService.updateNotificationPreferences(userId,
                request);
        return ResponseUtils.ok(response, "Notification preferences updated successfully");
    }

    /**
     * Reset current user's notification preferences to defaults
     * POST /api/v1/notifications/preferences/reset
     * Requires: Authenticated user
     * Returns: Reset NotificationPreferencesResponse with default values
     */
    @PostMapping("/reset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> resetNotificationPreferences() {
        Long userId = getCurrentUserId();
        log.info("Notification preferences reset request received for user ID: {}", userId);
        NotificationPreferencesResponse response = notificationPreferencesService.resetNotificationPreferences(userId);
        return ResponseUtils.ok(response, "Notification preferences reset to defaults");
    }
}
