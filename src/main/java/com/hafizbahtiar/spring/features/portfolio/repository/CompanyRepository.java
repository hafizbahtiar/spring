package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Company entity.
 * Provides CRUD operations and custom queries for company management.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Find all companies for a specific user, ordered by display order
     */
    List<Company> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all verified companies for a specific user, ordered by display order
     */
    List<Company> findByUserIdAndIsVerifiedTrueOrderByDisplayOrderAsc(Long userId);

    /**
     * Find companies by user ID and industry, ordered by display order
     */
    @Query("SELECT c FROM Company c WHERE c.user.id = :userId AND c.industry = :industry ORDER BY c.displayOrder ASC")
    List<Company> findByUserIdAndIndustryOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("industry") String industry);

    /**
     * Find verified companies by user ID and industry, ordered by display order
     */
    @Query("SELECT c FROM Company c WHERE c.user.id = :userId AND c.industry = :industry AND c.isVerified = true ORDER BY c.displayOrder ASC")
    List<Company> findByUserIdAndIndustryAndIsVerifiedTrueOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("industry") String industry);

    /**
     * Find company by user ID and company ID (for ownership validation)
     */
    Optional<Company> findByUserIdAndId(Long userId, Long id);

    /**
     * Count companies by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count verified companies by user ID
     */
    long countByUserIdAndIsVerifiedTrue(Long userId);

    /**
     * Count companies by user ID and industry
     */
    @Query("SELECT COUNT(c) FROM Company c WHERE c.user.id = :userId AND c.industry = :industry")
    long countByUserIdAndIndustry(@Param("userId") Long userId, @Param("industry") String industry);

    /**
     * Count verified companies by user ID and industry
     */
    @Query("SELECT COUNT(c) FROM Company c WHERE c.user.id = :userId AND c.industry = :industry AND c.isVerified = true")
    long countByUserIdAndIndustryAndIsVerifiedTrue(@Param("userId") Long userId, @Param("industry") String industry);

    /**
     * Find companies by user ID within a date range (by created date)
     */
    @Query("SELECT c FROM Company c WHERE c.user.id = :userId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.displayOrder ASC")
    List<Company> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find companies by name (case-insensitive search) for a specific user
     */
    @Query("SELECT c FROM Company c WHERE c.user.id = :userId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.displayOrder ASC")
    List<Company> findByUserIdAndNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("name") String name);
}
