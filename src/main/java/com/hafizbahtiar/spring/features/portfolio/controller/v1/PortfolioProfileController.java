package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileResponse;
import com.hafizbahtiar.spring.features.portfolio.service.PortfolioProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for portfolio profile management endpoints.
 * Handles portfolio profile CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/portfolio/profile")
@RequiredArgsConstructor
@Slf4j
public class PortfolioProfileController {

    private final PortfolioProfileService portfolioProfileService;

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
     * Get current user's portfolio profile
     * GET /api/v1/portfolio/profile
     * Requires: Authenticated user
     * Returns: PortfolioProfileResponse (creates default profile if doesn't exist)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioProfileResponse>> getPortfolioProfile() {
        Long userId = getCurrentUserId();
        log.info("Portfolio profile fetch request received for user ID: {}", userId);
        PortfolioProfileResponse response = portfolioProfileService.getPortfolioProfile(userId);
        return ResponseUtils.ok(response);
    }

    /**
     * Update current user's portfolio profile
     * PUT /api/v1/portfolio/profile
     * Requires: Authenticated user
     * Body: PortfolioProfileRequest (all fields optional for partial updates)
     * Returns: Updated PortfolioProfileResponse
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioProfileResponse>> updatePortfolioProfile(
            @Valid @RequestBody PortfolioProfileRequest request) {
        Long userId = getCurrentUserId();
        log.info("Portfolio profile update request received for user ID: {}", userId);
        PortfolioProfileResponse response = portfolioProfileService.updatePortfolioProfile(userId, request);
        return ResponseUtils.ok(response, "Portfolio profile updated successfully");
    }

    /**
     * Upload avatar URL for current user's portfolio profile
     * POST /api/v1/portfolio/profile/avatar
     * Requires: Authenticated user
     * Body: { "avatarUrl": "https://..." }
     * Returns: Updated PortfolioProfileResponse
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioProfileResponse>> uploadAvatar(
            @Valid @RequestBody AvatarUploadRequest request) {
        Long userId = getCurrentUserId();
        log.info("Avatar upload request received for user ID: {}, avatarUrl: {}", userId, request.getAvatarUrl());
        PortfolioProfileResponse response = portfolioProfileService.uploadAvatar(userId, request.getAvatarUrl());
        return ResponseUtils.ok(response, "Avatar updated successfully");
    }

    /**
     * Upload resume URL for current user's portfolio profile
     * POST /api/v1/portfolio/profile/resume
     * Requires: Authenticated user
     * Body: { "resumeUrl": "https://..." }
     * Returns: Updated PortfolioProfileResponse
     */
    @PostMapping("/resume")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioProfileResponse>> uploadResume(
            @Valid @RequestBody ResumeUploadRequest request) {
        Long userId = getCurrentUserId();
        log.info("Resume upload request received for user ID: {}, resumeUrl: {}", userId, request.getResumeUrl());
        PortfolioProfileResponse response = portfolioProfileService.uploadResume(userId, request.getResumeUrl());
        return ResponseUtils.ok(response, "Resume updated successfully");
    }

    /**
     * Request DTO for avatar upload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvatarUploadRequest {
        @NotBlank(message = "Avatar URL is required")
        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        private String avatarUrl;
    }

    /**
     * Request DTO for resume upload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeUploadRequest {
        @NotBlank(message = "Resume URL is required")
        @Size(max = 500, message = "Resume URL must not exceed 500 characters")
        private String resumeUrl;
    }
}
