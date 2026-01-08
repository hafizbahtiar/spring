package com.hafizbahtiar.spring.features.user.repository.mongodb;

import com.hafizbahtiar.spring.features.user.model.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for user activity logs.
 * Provides query methods for user activity analysis and audit trails.
 */
@Repository
public interface UserActivityRepository extends MongoRepository<UserActivity, String> {

    /**
     * Find all activities for a specific user
     */
    List<UserActivity> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find activities by activity type
     */
    List<UserActivity> findByActivityTypeOrderByTimestampDesc(String activityType);

    /**
     * Find activities for a specific user and activity type
     */
    List<UserActivity> findByUserIdAndActivityTypeOrderByTimestampDesc(Long userId, String activityType);

    /**
     * Find activities within a date range
     */
    List<UserActivity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Find activities for a specific user within a date range
     */
    List<UserActivity> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find activities by session ID
     */
    List<UserActivity> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Count activities by type for a user
     */
    long countByUserIdAndActivityType(Long userId, String activityType);

    /**
     * Find recent activities for a user (last N activities)
     */
    List<UserActivity> findTop10ByUserIdOrderByTimestampDesc(Long userId);
}
