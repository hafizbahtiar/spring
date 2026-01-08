package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.ProficiencyLevel;
import com.hafizbahtiar.spring.features.portfolio.entity.Skill;
import com.hafizbahtiar.spring.features.portfolio.entity.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Skill entity.
 * Provides CRUD operations and custom queries for skill management.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Find all skills for a specific user, ordered by display order
     */
    List<Skill> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all skills for a specific user with pagination, ordered by display order
     * ascending
     * (for consistent ordering in paginated results)
     */
    Page<Skill> findByUserIdOrderByDisplayOrderAsc(Long userId, Pageable pageable);

    /**
     * Find all active skills for a specific user, ordered by display order
     */
    List<Skill> findByUserIdAndIsActiveTrueOrderByDisplayOrderAsc(Long userId);

    /**
     * Find skills by user ID and category, ordered by display order
     */
    List<Skill> findByUserIdAndCategoryOrderByDisplayOrderAsc(Long userId, SkillCategory category);

    /**
     * Find active skills by user ID and category, ordered by display order
     */
    List<Skill> findByUserIdAndCategoryAndIsActiveTrueOrderByDisplayOrderAsc(Long userId, SkillCategory category);

    /**
     * Find skills by user ID and proficiency level, ordered by display order
     */
    List<Skill> findByUserIdAndProficiencyOrderByDisplayOrderAsc(Long userId, ProficiencyLevel proficiency);

    /**
     * Find active skills by user ID and proficiency level, ordered by display order
     */
    List<Skill> findByUserIdAndProficiencyAndIsActiveTrueOrderByDisplayOrderAsc(Long userId,
            ProficiencyLevel proficiency);

    /**
     * Find skills by user ID, category, and proficiency level, ordered by display
     * order
     */
    List<Skill> findByUserIdAndCategoryAndProficiencyOrderByDisplayOrderAsc(
            Long userId, SkillCategory category, ProficiencyLevel proficiency);

    /**
     * Find active skills by user ID, category, and proficiency level, ordered by
     * display order
     */
    List<Skill> findByUserIdAndCategoryAndProficiencyAndIsActiveTrueOrderByDisplayOrderAsc(
            Long userId, SkillCategory category, ProficiencyLevel proficiency);

    /**
     * Find skill by user ID and skill ID (for ownership validation)
     */
    Optional<Skill> findByUserIdAndId(Long userId, Long id);

    /**
     * Count skills by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count active skills by user ID
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Count skills by user ID and category
     */
    long countByUserIdAndCategory(Long userId, SkillCategory category);

    /**
     * Count active skills by user ID and category
     */
    long countByUserIdAndCategoryAndIsActiveTrue(Long userId, SkillCategory category);

    /**
     * Count skills by user ID and proficiency level
     */
    long countByUserIdAndProficiency(Long userId, ProficiencyLevel proficiency);

    /**
     * Count active skills by user ID and proficiency level
     */
    long countByUserIdAndProficiencyAndIsActiveTrue(Long userId, ProficiencyLevel proficiency);

    /**
     * Find skills by user ID within a date range (by created date)
     */
    @Query("SELECT s FROM Skill s WHERE s.user.id = :userId AND s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.displayOrder ASC")
    List<Skill> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find skills by name (case-insensitive search) for a specific user
     */
    @Query("SELECT s FROM Skill s WHERE s.user.id = :userId AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY s.displayOrder ASC")
    List<Skill> findByUserIdAndNameContainingIgnoreCase(@Param("userId") Long userId, @Param("name") String name);
}
