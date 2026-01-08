package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesResponse;
import com.hafizbahtiar.spring.features.user.service.CurrencyPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for currency preferences management endpoints.
 * Handles currency preferences CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/settings/currency/preferences")
@RequiredArgsConstructor
@Slf4j
public class CurrencyPreferencesController {

    private final CurrencyPreferencesService currencyPreferencesService;

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
     * Get current user's currency preferences
     * GET /api/v1/settings/currency/preferences
     * Requires: Authenticated user
     * Returns: CurrencyPreferencesResponse (creates default preferences if doesn't
     * exist)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CurrencyPreferencesResponse>> getCurrencyPreferences() {
        Long userId = getCurrentUserId();
        log.info("Currency preferences fetch request received for user ID: {}", userId);
        CurrencyPreferencesResponse response = currencyPreferencesService.getCurrencyPreferences(userId);
        return ResponseUtils.ok(response);
    }

    /**
     * Update current user's currency preferences
     * PUT /api/v1/settings/currency/preferences
     * Requires: Authenticated user
     * Body: CurrencyPreferencesRequest
     * Returns: Updated CurrencyPreferencesResponse
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CurrencyPreferencesResponse>> updateCurrencyPreferences(
            @Valid @RequestBody CurrencyPreferencesRequest request) {
        Long userId = getCurrentUserId();
        log.info("Currency preferences update request received for user ID: {}", userId);
        CurrencyPreferencesResponse response = currencyPreferencesService.updateCurrencyPreferences(userId, request);
        return ResponseUtils.ok(response, "Currency preferences updated successfully");
    }
}
