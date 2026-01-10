package com.hafizbahtiar.spring.features.user.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.user.dto.UpdateProfileRequest;
import com.hafizbahtiar.spring.features.user.dto.UserProfileResponse;
import com.hafizbahtiar.spring.features.user.dto.UserRegistrationRequest;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.dto.UserUpdateRequest;
import com.hafizbahtiar.spring.features.user.service.UserActivityLoggingService;
import com.hafizbahtiar.spring.features.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hafizbahtiar.spring.common.security.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserActivityLoggingService userActivityLoggingService;

    /**
     * Register a new user
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration request received for email: {}", request.getEmail());
        UserResponse response = userService.register(request);
        return ResponseUtils.created(response, "User registered successfully");
    }

    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     * Requires: Authenticated user
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id, HttpServletRequest request) {
        log.debug("Fetching user with ID: {}", id);
        long startTime = System.currentTimeMillis();
        UserResponse response = userService.getById(id);
        long responseTime = System.currentTimeMillis() - startTime;

        // Log profile view activity
        userActivityLoggingService.logProfileView(id, request, responseTime);

        return ResponseUtils.ok(response);
    }

    /**
     * Get user profile by ID
     * GET /api/v1/users/{id}/profile
     * Requires: Authenticated user
     */
    @GetMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long id,
            HttpServletRequest request) {
        log.debug("Fetching user profile with ID: {}", id);
        long startTime = System.currentTimeMillis();
        UserProfileResponse response = userService.getProfileById(id);
        long responseTime = System.currentTimeMillis() - startTime;

        // Log profile view activity
        userActivityLoggingService.logProfileView(id, request, responseTime);

        return ResponseUtils.ok(response);
    }

    /**
     * Get all active users
     * GET /api/v1/users
     * Requires: OWNER or ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.debug("Fetching all active users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseUtils.ok(users);
    }

    /**
     * Update user information
     * PUT /api/v1/users/{id}
     * Requires: User can update own profile OR OWNER/ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.ownsResource(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Update request received for user ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseUtils.ok(response, "User updated successfully");
    }

    /**
     * Delete (deactivate) a user
     * DELETE /api/v1/users/{id}
     * Requires: OWNER or ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Delete request received for user ID: {}", id);
        userService.deleteUser(id);
        return ResponseUtils.noContent();
    }

    /**
     * Verify user email
     * POST /api/v1/users/{id}/verify
     * Requires: User can verify own email OR OWNER/ADMIN role
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.ownsResource(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(@PathVariable Long id) {
        log.info("Email verification request received for user ID: {}", id);
        UserResponse response = userService.verifyEmail(id);
        return ResponseUtils.ok(response, "Email verified successfully");
    }

    /**
     * Check if email exists
     * GET /api/v1/users/check/email?email={email}
     */
    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        log.debug("Checking if email exists: {}", email);
        boolean exists = userService.emailExists(email);
        return ResponseUtils.ok(exists);
    }

    /**
     * Check if username exists
     * GET /api/v1/users/check/username?username={username}
     */
    @GetMapping("/check/username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(@RequestParam String username) {
        log.debug("Checking if username exists: {}", username);
        boolean exists = userService.usernameExists(username);
        return ResponseUtils.ok(exists);
    }

    /**
     * Get user by username (public endpoint for portfolio contact forms)
     * GET /api/v1/users/by-username/{username}
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        log.debug("Getting user by username: {}", username);
        UserResponse user = userService.getUserByUsername(username);
        return ResponseUtils.ok(user);
    }

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
     * Update user profile
     * PATCH /api/v1/users/profile
     * Requires: Authenticated user
     * Body: UpdateProfileRequest (firstName, lastName, bio, location, website)
     * Returns: Updated UserResponse
     */
    @PatchMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Profile update request received for user ID: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseUtils.ok(response, "Profile updated successfully");
    }

    /**
     * Upload avatar for current user
     * POST /api/v1/users/profile/avatar
     * Requires: Authenticated user
     * Supports two methods:
     * 1. File upload: multipart/form-data with "file" field
     * 2. URL upload: application/json with "avatarUrl" field
     * Returns: Updated UserResponse
     */
    @PostMapping("/profile/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String avatarUrl,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Avatar upload request received for user ID: {}", userId);

        // Validate that at least one is provided
        if ((file == null || file.isEmpty()) && (avatarUrl == null || avatarUrl.trim().isEmpty())) {
            throw new IllegalArgumentException("Either 'file' or 'avatarUrl' must be provided");
        }

        UserResponse response = userService.uploadAvatar(userId, avatarUrl, file);
        return ResponseUtils.ok(response, "Avatar updated successfully");
    }
}
