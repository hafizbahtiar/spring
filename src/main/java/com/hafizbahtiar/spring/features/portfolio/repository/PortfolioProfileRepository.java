package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.PortfolioProfile;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * @param userId User ID
     * @return Optional PortfolioProfile
     */
    Optional<PortfolioProfile> findByUserId(Long userId);

    /**
     * Check if portfolio profile exists for a user.
     *
     * @param userId User ID
     * @return true if profile exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}

