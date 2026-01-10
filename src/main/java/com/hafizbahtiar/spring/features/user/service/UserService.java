package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.UserProfileResponse;
import com.hafizbahtiar.spring.features.user.dto.UserRegistrationRequest;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    /**
     * Register a new user
     *
     * @param request Registration request containing user details
     * @return UserResponse with created user information
     */
    UserResponse register(UserRegistrationRequest request);

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return UserResponse
     */
    UserResponse getById(Long id);

    /**
     * Get user by username
     *
     * @param username Username
     * @return UserResponse
     */
    UserResponse getUserByUsername(String username);

    /**
     * Get user profile by ID
     *
     * @param id User ID
     * @return UserProfileResponse
     */
    UserProfileResponse getProfileById(Long id);

    /**
     * Get all active users
     *
     * @return List of UserResponse
     */
    List<UserResponse> getAllUsers();

    /**
     * Update user information
     *
     * @param id      User ID
     * @param request Update request containing fields to update
     * @return Updated UserResponse
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * Delete (deactivate) a user
     *
     * @param id User ID
     */
    void deleteUser(Long id);

    /**
     * Verify user email
     *
     * @param id User ID
     * @return Updated UserResponse
     */
    UserResponse verifyEmail(Long id);

    /**
     * Check if email exists
     *
     * @param email Email to check
     * @return true if email exists
     */
    boolean emailExists(String email);

    /**
     * Check if username exists
     *
     * @param username Username to check
     * @return true if username exists
     */
    boolean usernameExists(String username);

    /**
     * Update user role with OWNER uniqueness validation.
     * This method validates that only one user can have the OWNER role at a time.
     *
     * @param userId  User ID to update
     * @param newRole New role to assign (USER, OWNER, ADMIN)
     * @return Updated UserResponse
     */
    UserResponse updateUserRole(Long userId, String newRole);

    /**
     * Update user profile (firstName, lastName, bio, location, website).
     *
     * @param userId  User ID
     * @param request Update profile request
     * @return Updated UserResponse
     */
    UserResponse updateProfile(Long userId, com.hafizbahtiar.spring.features.user.dto.UpdateProfileRequest request);

    /**
     * Upload avatar for user (via URL or file).
     *
     * @param userId    User ID
     * @param avatarUrl Avatar URL (if provided)
     * @param file      MultipartFile (if provided, optional)
     * @return Updated UserResponse
     */
    UserResponse uploadAvatar(Long userId, String avatarUrl, org.springframework.web.multipart.MultipartFile file);

    /**
     * Get paginated list of users with filters (for admin).
     *
     * @param search   Search term (email, username, firstName, lastName)
     * @param role     Filter by role
     * @param active   Filter by active status
     * @param pageable Pagination parameters
     * @return Page of UserResponse
     */
    Page<UserResponse> getUsersWithFilters(String search, String role, Boolean active, Pageable pageable);

    /**
     * Create a new user (admin only).
     *
     * @param email     User email
     * @param username  User username
     * @param password  User password (will be hashed)
     * @param firstName First name
     * @param lastName  Last name
     * @param phone     Phone number
     * @param role      User role (USER or ADMIN, not OWNER)
     * @param active    Active status
     * @return Created UserResponse
     */
    UserResponse createUser(String email, String username, String password, String firstName, String lastName,
            String phone, String role, Boolean active);

    /**
     * Update user active status.
     *
     * @param id     User ID
     * @param active Active status
     * @return Updated UserResponse
     */
    UserResponse updateUserActiveStatus(Long id, Boolean active);
}
