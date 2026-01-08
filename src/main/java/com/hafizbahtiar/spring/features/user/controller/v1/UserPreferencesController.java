package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.UserPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.UserPreferencesResponse;
import com.hafizbahtiar.spring.features.user.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hafizbahtiar.spring.common.security.UserPrincipal;

/**
 * REST controller for user preferences management endpoints.
 * Handles user preferences CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/settings/preferences")
@RequiredArgsConstructor
@Slf4j
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

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
     * Get current user's preferences
     * GET /api/v1/settings/preferences
     * Requires: Authenticated user
     * Returns: UserPreferencesResponse (creates default preferences if doesn't exist)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> getUserPreferences() {
        Long userId = getCurrentUserId();
        log.info("User preferences fetch request received for user ID: {}", userId);
        UserPreferencesResponse response = userPreferencesService.getUserPreferences(userId);
        return ResponseUtils.ok(response);
    }

    /**
     * Update current user's preferences
     * PUT /api/v1/settings/preferences
     * Requires: Authenticated user
     * Body: UserPreferencesRequest (all fields optional for partial updates)
     * Returns: Updated UserPreferencesResponse
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> updateUserPreferences(
            @Valid @RequestBody UserPreferencesRequest request) {
        Long userId = getCurrentUserId();
        log.info("User preferences update request received for user ID: {}", userId);
        UserPreferencesResponse response = userPreferencesService.updateUserPreferences(userId, request);
        return ResponseUtils.ok(response, "Preferences updated successfully");
    }

    /**
     * Reset current user's preferences to defaults
     * POST /api/v1/settings/preferences/reset
     * Requires: Authenticated user
     * Returns: Reset UserPreferencesResponse with default values
     */
    @PostMapping("/reset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> resetUserPreferences() {
        Long userId = getCurrentUserId();
        log.info("User preferences reset request received for user ID: {}", userId);
        UserPreferencesResponse response = userPreferencesService.resetUserPreferences(userId);
        return ResponseUtils.ok(response, "Preferences reset to defaults");
    }
}

