package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.SecurityUtils;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.UserActivityResponse;
import com.hafizbahtiar.spring.features.user.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for user activity logs.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserActivityController {

    private final UserActivityService userActivityService;

    /**
     * Get current user's recent activities
     * GET /api/v1/users/me/activity?limit=5
     * Requires: Authenticated user
     */
    @GetMapping("/me/activity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserActivityResponse>>> getMyActivity(
            @RequestParam(defaultValue = "5") int limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Fetching recent activities for current user ID: {} with limit: {}", userId, limit);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 5; // Default to 5 if invalid
        }

        List<UserActivityResponse> activities = userActivityService.getRecentActivities(userId, limit);
        return ResponseUtils.ok(activities);
    }

    /**
     * Get user activities by user ID
     * GET /api/v1/users/{userId}/activity?limit=10&type={activityType}
     * Requires: User can access own activities OR OWNER/ADMIN role
     */
    @GetMapping("/{userId}/activity")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.ownsResource(#userId)")
    public ResponseEntity<ApiResponse<List<UserActivityResponse>>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String type) {
        log.debug("Fetching activities for user ID: {} with limit: {} and type: {}", userId, limit, type);

        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 10; // Default to 10 if invalid
        }

        List<UserActivityResponse> activities;
        if (type != null && !type.trim().isEmpty()) {
            activities = userActivityService.getActivitiesByType(userId, type.trim(), limit);
        } else {
            activities = userActivityService.getRecentActivities(userId, limit);
        }

        return ResponseUtils.ok(activities);
    }
}
