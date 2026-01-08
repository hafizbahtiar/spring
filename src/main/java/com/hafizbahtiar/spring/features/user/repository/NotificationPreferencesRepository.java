package com.hafizbahtiar.spring.features.user.repository;

import com.hafizbahtiar.spring.features.user.entity.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for NotificationPreferences entity.
 * Provides CRUD operations for notification preferences management.
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {

    /**
     * Find notification preferences by user ID.
     *
     * @param userId User ID
     * @return Optional NotificationPreferences
     */
    Optional<NotificationPreferences> findByUserId(Long userId);

    /**
     * Check if notification preferences exist for a user.
     *
     * @param userId User ID
     * @return true if preferences exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}
