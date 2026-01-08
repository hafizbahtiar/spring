package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.DegreeType;
import com.hafizbahtiar.spring.features.portfolio.entity.Education;
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
 * Repository for Education entity.
 * Provides CRUD operations and custom queries for education management.
 */
@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    /**
     * Find all educations for a specific user, ordered by start date descending
     * (most recent first)
     */
    List<Education> findByUserIdOrderByStartDateDesc(Long userId);

    /**
     * Find all educations for a specific user with pagination, ordered by display
     * order ascending
     * (for consistent ordering in paginated results)
     */
    Page<Education> findByUserIdOrderByDisplayOrderAsc(Long userId, Pageable pageable);

    /**
     * Find all active (current) educations for a specific user, ordered by start
     * date descending
     */
    List<Education> findByUserIdAndIsCurrentTrueOrderByStartDateDesc(Long userId);

    /**
     * Find all completed (non-current) educations for a specific user, ordered by
     * start date descending
     */
    List<Education> findByUserIdAndIsCurrentFalseOrderByStartDateDesc(Long userId);

    /**
     * Find educations by user ID and degree type, ordered by start date descending
     */
    List<Education> findByUserIdAndDegreeOrderByStartDateDesc(Long userId, DegreeType degree);

    /**
     * Find current educations by user ID and degree type, ordered by start date
     * descending
     */
    List<Education> findByUserIdAndDegreeAndIsCurrentTrueOrderByStartDateDesc(Long userId, DegreeType degree);

    /**
     * Find educations by user ID within a date range (by start date)
     */
    @Query("SELECT e FROM Education e WHERE e.user.id = :userId " +
            "AND e.startDate BETWEEN :startDate AND :endDate ORDER BY e.startDate DESC")
    List<Education> findByUserIdAndStartDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find educations by user ID that overlap with a date range
     */
    @Query("SELECT e FROM Education e WHERE e.user.id = :userId " +
            "AND ((e.startDate <= :endDate AND (e.endDate IS NULL OR e.endDate >= :startDate))) " +
            "ORDER BY e.startDate DESC")
    List<Education> findByUserIdAndDateRangeOverlap(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find education by user ID and education ID (for ownership validation)
     */
    Optional<Education> findByUserIdAndId(Long userId, Long id);

    /**
     * Count educations by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count current educations by user ID
     */
    long countByUserIdAndIsCurrentTrue(Long userId);

    /**
     * Count educations by user ID and degree type
     */
    long countByUserIdAndDegree(Long userId, DegreeType degree);

    /**
     * Count higher education degrees by user ID (BACHELOR, MASTER, DOCTORATE)
     */
    @Query("SELECT COUNT(e) FROM Education e WHERE e.user.id = :userId " +
            "AND e.degree IN ('BACHELOR', 'MASTER', 'DOCTORATE')")
    long countHigherEducationByUserId(@Param("userId") Long userId);

    /**
     * Find educations by user ID ordered by display order
     */
    List<Education> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find educations by institution name (case-insensitive search) for a specific
     * user
     */
    @Query("SELECT e FROM Education e WHERE e.user.id = :userId " +
            "AND LOWER(e.institution) LIKE LOWER(CONCAT('%', :institution, '%')) ORDER BY e.startDate DESC")
    List<Education> findByUserIdAndInstitutionContainingIgnoreCase(
            @Param("userId") Long userId, @Param("institution") String institution);

    /**
     * Find educations by field of study (case-insensitive search) for a specific
     * user
     */
    @Query("SELECT e FROM Education e WHERE e.user.id = :userId " +
            "AND LOWER(e.fieldOfStudy) LIKE LOWER(CONCAT('%', :fieldOfStudy, '%')) ORDER BY e.startDate DESC")
    List<Education> findByUserIdAndFieldOfStudyContainingIgnoreCase(
            @Param("userId") Long userId, @Param("fieldOfStudy") String fieldOfStudy);
}
