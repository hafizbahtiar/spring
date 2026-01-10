package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.PortfolioProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PortfolioProfile entity.
 * Provides CRUD operations for portfolio profile management.
 */
@Repository
public interface PortfolioProfileRepository extends JpaRepository<PortfolioProfile, Long> {

    /**
     * Find portfolio profile by user ID.
     *
     * @param userId User ID (Long)
     * @return Optional PortfolioProfile
     */
    Optional<PortfolioProfile> findByUserId(Long userId);

    /**
     * Find portfolio profile by user UUID.
     *
     * @param userUuid User UUID
     * @return Optional PortfolioProfile
     */
    @Query("SELECT p FROM PortfolioProfile p WHERE p.user.uuid = :userUuid")
    Optional<PortfolioProfile> findByUserUuid(@Param("userUuid") java.util.UUID userUuid);

    /**
     * Check if portfolio profile exists for a user.
     *
     * @param userId User ID (Long)
     * @return true if profile exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}

