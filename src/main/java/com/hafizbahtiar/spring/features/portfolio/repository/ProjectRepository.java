package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.PlatformType;
import com.hafizbahtiar.spring.features.portfolio.entity.Project;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectStatus;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectType;
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
 * Repository for Project entity.
 * Provides CRUD operations and custom queries for project management.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

        /**
         * Find all projects for a specific user, ordered by display order
         */
        List<Project> findByUserIdOrderByDisplayOrderAsc(Long userId);

        /**
         * Find all projects for a specific user with pagination, ordered by display
         * order
         * ascending
         * (for consistent ordering in paginated results)
         */
        Page<Project> findByUserIdOrderByDisplayOrderAsc(Long userId, Pageable pageable);

        /**
         * Find all projects for a specific user, ordered by start date descending
         */
        List<Project> findByUserIdOrderByStartDateDesc(Long userId);

        /**
         * Find projects by user ID and status, ordered by display order
         */
        List<Project> findByUserIdAndStatusOrderByDisplayOrderAsc(Long userId, ProjectStatus status);

        /**
         * Find projects by user ID and status, ordered by start date descending
         */
        List<Project> findByUserIdAndStatusOrderByStartDateDesc(Long userId, ProjectStatus status);

        /**
         * Find projects by user ID and type, ordered by display order
         */
        List<Project> findByUserIdAndTypeOrderByDisplayOrderAsc(Long userId, ProjectType type);

        /**
         * Find projects by user ID and type, ordered by start date descending
         */
        List<Project> findByUserIdAndTypeOrderByStartDateDesc(Long userId, ProjectType type);

        /**
         * Find projects by user ID, type, and status, ordered by display order
         */
        List<Project> findByUserIdAndTypeAndStatusOrderByDisplayOrderAsc(
                        Long userId, ProjectType type, ProjectStatus status);

        /**
         * Find projects by user ID and platform, ordered by display order
         */
        List<Project> findByUserIdAndPlatformOrderByDisplayOrderAsc(Long userId, PlatformType platform);

        /**
         * Find projects by user ID and platform, ordered by start date descending
         */
        List<Project> findByUserIdAndPlatformOrderByStartDateDesc(Long userId, PlatformType platform);

        /**
         * Find projects by user ID, platform, and status, ordered by display order
         */
        List<Project> findByUserIdAndPlatformAndStatusOrderByDisplayOrderAsc(
                        Long userId, PlatformType platform, ProjectStatus status);

        /**
         * Find projects by user ID, type, and platform, ordered by display order
         */
        List<Project> findByUserIdAndTypeAndPlatformOrderByDisplayOrderAsc(
                        Long userId, ProjectType type, PlatformType platform);

        /**
         * Find featured projects for a specific user, ordered by display order
         */
        List<Project> findByUserIdAndIsFeaturedTrueOrderByDisplayOrderAsc(Long userId);

        /**
         * Find featured projects for a specific user, ordered by start date descending
         */
        List<Project> findByUserIdAndIsFeaturedTrueOrderByStartDateDesc(Long userId);

        /**
         * Find projects by user ID within a date range (by start date)
         */
        @Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
                        "AND p.startDate BETWEEN :startDate AND :endDate ORDER BY p.displayOrder ASC")
        List<Project> findByUserIdAndStartDateBetween(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find projects by user ID that overlap with a date range
         */
        @Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
                        "AND ((p.startDate <= :endDate AND (p.endDate IS NULL OR p.endDate >= :startDate))) " +
                        "ORDER BY p.displayOrder ASC")
        List<Project> findByUserIdAndDateRangeOverlap(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Search projects by title or description (case-insensitive, full-text search)
         */
        @Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
                        "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY p.displayOrder ASC")
        List<Project> searchByUserIdAndTitleOrDescription(
                        @Param("userId") Long userId, @Param("searchTerm") String searchTerm);

        /**
         * Search projects by title or description (case-insensitive, paginated)
         */
        @Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
                        "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY p.displayOrder ASC")
        Page<Project> searchByUserIdAndTitleOrDescription(
                        @Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Find project by user ID and project ID (for ownership validation)
         */
        Optional<Project> findByUserIdAndId(Long userId, Long id);

        /**
         * Count projects by user ID
         */
        long countByUserId(Long userId);

        /**
         * Count projects by user ID and status
         */
        long countByUserIdAndStatus(Long userId, ProjectStatus status);

        /**
         * Count projects by user ID and type
         */
        long countByUserIdAndType(Long userId, ProjectType type);

        /**
         * Count featured projects by user ID
         */
        long countByUserIdAndIsFeaturedTrue(Long userId);

        /**
         * Count projects by user ID, type, and status
         */
        long countByUserIdAndTypeAndStatus(Long userId, ProjectType type, ProjectStatus status);

        /**
         * Count projects by user ID and platform
         */
        long countByUserIdAndPlatform(Long userId, PlatformType platform);

        /**
         * Count projects by user ID, platform, and status
         */
        long countByUserIdAndPlatformAndStatus(Long userId, PlatformType platform, ProjectStatus status);

        /**
         * Find projects by user ID ordered by display order (for reordering)
         */
        List<Project> findByUserIdOrderByDisplayOrderAscCreatedAtAsc(Long userId);
}
