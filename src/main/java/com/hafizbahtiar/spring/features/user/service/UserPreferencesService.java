package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.UserPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.UserPreferencesResponse;

/**
 * Service interface for user preferences management.
 * Handles CRUD operations for user's application preferences.
 */
public interface UserPreferencesService {

    /**
     * Get user preferences for a user.
     * Creates default preferences if one doesn't exist.
     *
     * @param userId User ID
     * @return UserPreferencesResponse
     */
    UserPreferencesResponse getUserPreferences(Long userId);

    /**
     * Update user preferences for a user.
     * Creates preferences if one doesn't exist.
     *
     * @param userId  User ID
     * @param request Update request (all fields optional for partial updates)
     * @return Updated UserPreferencesResponse
     */
    UserPreferencesResponse updateUserPreferences(Long userId, UserPreferencesRequest request);

    /**
     * Reset user preferences to default values.
     *
     * @param userId User ID
     * @return Reset UserPreferencesResponse with default values
     */
    UserPreferencesResponse resetUserPreferences(Long userId);
}
