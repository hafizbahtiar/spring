package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Blog entity.
 * Provides CRUD operations and custom queries for blog management.
 */
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

        /**
         * Find all blogs for a specific user with pagination.
         * Sorting is handled via Pageable parameter.
         */
        Page<Blog> findByUserId(Long userId, Pageable pageable);

        /**
         * Find published blogs for a specific user with pagination.
         * Sorting is handled via Pageable parameter.
         */
        Page<Blog> findByUserIdAndPublishedTrue(Long userId, Pageable pageable);

        /**
         * Find draft blogs for a specific user with pagination.
         * Sorting is handled via Pageable parameter.
         */
        Page<Blog> findByUserIdAndPublishedFalse(Long userId, Pageable pageable);

        /**
         * Find blog by user ID and blog ID
         */
        Optional<Blog> findByUserIdAndId(Long userId, Long id);

        /**
         * Find blog by slug (for public access)
         */
        Optional<Blog> findBySlug(String slug);

        /**
         * Check if slug exists (for validation)
         */
        boolean existsBySlug(String slug);

        /**
         * Check if slug exists for a different blog (for update validation)
         */
        @Query("SELECT COUNT(b) > 0 FROM Blog b WHERE b.slug = :slug AND b.id != :id")
        boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("id") Long id);

        /**
         * Search blogs by title or content for a specific user
         */
        @Query("SELECT b FROM Blog b WHERE b.user.id = :userId AND " +
                        "(LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(b.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Blog> searchByUserIdAndSearchTerm(@Param("userId") Long userId,
                        @Param("searchTerm") String searchTerm, Pageable pageable);
}
