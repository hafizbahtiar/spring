package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesResponse;

/**
 * Service interface for notification preferences management.
 * Handles CRUD operations for user's notification preferences.
 */
public interface NotificationPreferencesService {

    /**
     * Get notification preferences for a user.
     * Creates default preferences if one doesn't exist.
     *
     * @param userId User ID
     * @return NotificationPreferencesResponse
     */
    NotificationPreferencesResponse getNotificationPreferences(Long userId);

    /**
     * Update notification preferences for a user.
     * Creates preferences if one doesn't exist.
     *
     * @param userId  User ID
     * @param request Update request (all fields optional for partial updates)
     * @return Updated NotificationPreferencesResponse
     */
    NotificationPreferencesResponse updateNotificationPreferences(Long userId, NotificationPreferencesRequest request);

    /**
     * Reset notification preferences to default values.
     *
     * @param userId User ID
     * @return Reset NotificationPreferencesResponse with default values
     */
    NotificationPreferencesResponse resetNotificationPreferences(Long userId);
}
