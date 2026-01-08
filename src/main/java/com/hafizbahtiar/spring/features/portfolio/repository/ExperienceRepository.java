package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.EmploymentType;
import com.hafizbahtiar.spring.features.portfolio.entity.Experience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Experience entity.
 * Provides CRUD operations and custom queries for work experience management.
 */
@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    /**
     * Find all experiences for a specific user, ordered by start date descending
     * (most recent first)
     */
    List<Experience> findByUserIdOrderByStartDateDesc(Long userId);

    /**
     * Find all active (current) experiences for a specific user, ordered by start
     * date descending
     */
    List<Experience> findByUserIdAndIsCurrentTrueOrderByStartDateDesc(Long userId);

    /**
     * Find all past (non-current) experiences for a specific user, ordered by start
     * date descending
     */
    List<Experience> findByUserIdAndIsCurrentFalseOrderByStartDateDesc(Long userId);

    /**
     * Find experiences by user ID and employment type, ordered by start date
     * descending
     */
    List<Experience> findByUserIdAndEmploymentTypeOrderByStartDateDesc(Long userId, EmploymentType employmentType);

    /**
     * Find current experiences by user ID and employment type, ordered by start
     * date descending
     */
    List<Experience> findByUserIdAndEmploymentTypeAndIsCurrentTrueOrderByStartDateDesc(
            Long userId, EmploymentType employmentType);

    /**
     * Find experiences by user ID within a date range (by start date)
     */
    @Query("SELECT e FROM Experience e WHERE e.user.id = :userId " +
            "AND e.startDate BETWEEN :startDate AND :endDate ORDER BY e.startDate DESC")
    List<Experience> findByUserIdAndStartDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find experiences by user ID that overlap with a date range
     */
    @Query("SELECT e FROM Experience e WHERE e.user.id = :userId " +
            "AND ((e.startDate <= :endDate AND (e.endDate IS NULL OR e.endDate >= :startDate))) " +
            "ORDER BY e.startDate DESC")
    List<Experience> findByUserIdAndDateRangeOverlap(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find experience by user ID and experience ID (for ownership validation)
     */
    Optional<Experience> findByUserIdAndId(Long userId, Long id);

    /**
     * Count experiences by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count current experiences by user ID
     */
    long countByUserIdAndIsCurrentTrue(Long userId);

    /**
     * Count experiences by user ID and employment type
     */
    long countByUserIdAndEmploymentType(Long userId, EmploymentType employmentType);

    /**
     * Find experiences by user ID ordered by display order
     */
    List<Experience> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all experiences for a specific user with pagination, ordered by display
     * order ascending
     * (for consistent ordering in paginated results)
     */
    Page<Experience> findByUserIdOrderByDisplayOrderAsc(Long userId, Pageable pageable);

    /**
     * Find experiences by company name (case-insensitive search) for a specific
     * user
     */
    @Query("SELECT e FROM Experience e WHERE e.user.id = :userId " +
            "AND LOWER(e.company) LIKE LOWER(CONCAT('%', :company, '%')) ORDER BY e.startDate DESC")
    List<Experience> findByUserIdAndCompanyContainingIgnoreCase(
            @Param("userId") Long userId, @Param("company") String company);

    /**
     * Find experiences by position (case-insensitive search) for a specific user
     */
    @Query("SELECT e FROM Experience e WHERE e.user.id = :userId " +
            "AND LOWER(e.position) LIKE LOWER(CONCAT('%', :position, '%')) ORDER BY e.startDate DESC")
    List<Experience> findByUserIdAndPositionContainingIgnoreCase(
            @Param("userId") Long userId, @Param("position") String position);
}
