package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.features.user.dto.UserActivityResponse;

import java.util.List;

/**
 * Service interface for retrieving user activity logs.
 */
public interface UserActivityService {

    /**
     * Get recent activities for a user.
     *
     * @param userId User ID
     * @param limit  Maximum number of activities to return
     * @return List of recent activities
     */
    List<UserActivityResponse> getRecentActivities(Long userId, int limit);

    /**
     * Get activities for a user filtered by activity type.
     *
     * @param userId       User ID
     * @param activityType Activity type to filter by (e.g., "REGISTRATION",
     *                     "PROFILE_UPDATE")
     * @param limit        Maximum number of activities to return
     * @return List of filtered activities
     */
    List<UserActivityResponse> getActivitiesByType(Long userId, String activityType, int limit);
}
