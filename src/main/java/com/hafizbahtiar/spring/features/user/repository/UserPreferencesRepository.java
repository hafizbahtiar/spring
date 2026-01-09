package com.hafizbahtiar.spring.features.user.repository;

import com.hafizbahtiar.spring.features.user.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserPreferences entity.
 * Provides CRUD operations for user preferences management.
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    /**
     * Find user preferences by user ID.
     * Uses JOIN FETCH to eagerly load the User entity to avoid LazyInitializationException.
     *
     * @param userId User ID
     * @return Optional UserPreferences with User loaded
     */
    @Query("SELECT up FROM UserPreferences up JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserPreferences> findByUserId(@Param("userId") Long userId);

    /**
     * Check if user preferences exist for a user.
     *
     * @param userId User ID
     * @return true if preferences exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}
