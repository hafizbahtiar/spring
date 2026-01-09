package com.hafizbahtiar.spring.features.user.repository;

import com.hafizbahtiar.spring.features.user.entity.CurrencyPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CurrencyPreferences entity.
 * Provides CRUD operations for currency preferences management.
 */
@Repository
public interface CurrencyPreferencesRepository extends JpaRepository<CurrencyPreferences, Long> {

    /**
     * Find currency preferences by user ID.
     * Uses JOIN FETCH to eagerly load the User entity to avoid LazyInitializationException.
     *
     * @param userId User ID
     * @return Optional CurrencyPreferences with User loaded
     */
    @Query("SELECT cp FROM CurrencyPreferences cp JOIN FETCH cp.user WHERE cp.user.id = :userId")
    Optional<CurrencyPreferences> findByUserId(@Param("userId") Long userId);

    /**
     * Check if currency preferences exist for a user.
     *
     * @param userId User ID
     * @return true if preferences exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}
