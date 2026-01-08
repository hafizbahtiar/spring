package com.hafizbahtiar.spring.features.portfolio.repository;

import com.hafizbahtiar.spring.features.portfolio.entity.Testimonial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Testimonial entity.
 * Provides CRUD operations and custom queries for testimonial management.
 */
@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    /**
     * Find all testimonials for a specific user, ordered by display order
     */
    List<Testimonial> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all testimonials for a specific user with pagination, ordered by display
     * order ascending
     * (for consistent ordering in paginated results)
     */
    Page<Testimonial> findByUserIdOrderByDisplayOrderAsc(Long userId, Pageable pageable);

    /**
     * Find all approved testimonials for a specific user, ordered by display order
     */
    List<Testimonial> findByUserIdAndIsApprovedTrueOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all featured testimonials for a specific user, ordered by display order
     */
    List<Testimonial> findByUserIdAndIsFeaturedTrueOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all approved and featured testimonials for a specific user, ordered by
     * display order
     */
    List<Testimonial> findByUserIdAndIsApprovedTrueAndIsFeaturedTrueOrderByDisplayOrderAsc(Long userId);

    /**
     * Find testimonials by user ID and rating, ordered by display order
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.rating = :rating ORDER BY t.displayOrder ASC")
    List<Testimonial> findByUserIdAndRatingOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("rating") Integer rating);

    /**
     * Find testimonials by user ID and rating, ordered by rating (highest first)
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.rating = :rating ORDER BY t.rating DESC, t.displayOrder ASC")
    List<Testimonial> findByUserIdAndRatingOrderByRatingDesc(@Param("userId") Long userId,
            @Param("rating") Integer rating);

    /**
     * Find testimonials by user ID and project ID, ordered by display order
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.project.id = :projectId ORDER BY t.displayOrder ASC")
    List<Testimonial> findByUserIdAndProjectIdOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId);

    /**
     * Find approved testimonials by user ID and project ID, ordered by display
     * order
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.project.id = :projectId AND t.isApproved = true ORDER BY t.displayOrder ASC")
    List<Testimonial> findByUserIdAndProjectIdAndIsApprovedTrueOrderByDisplayOrderAsc(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId);

    /**
     * Find testimonial by user ID and testimonial ID (for ownership validation)
     */
    Optional<Testimonial> findByUserIdAndId(Long userId, Long id);

    /**
     * Find all approved testimonials (for public display), ordered by display order
     * Used for public portfolio view
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.isApproved = true ORDER BY t.displayOrder ASC")
    List<Testimonial> findApprovedByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * Find all approved testimonials (for public display), ordered by rating
     * (highest first)
     * Used for public portfolio view
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND t.isApproved = true ORDER BY t.rating DESC, t.displayOrder ASC")
    List<Testimonial> findApprovedByUserIdOrderByRatingDesc(Long userId);

    /**
     * Count testimonials by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count approved testimonials by user ID
     */
    long countByUserIdAndIsApprovedTrue(Long userId);

    /**
     * Count featured testimonials by user ID
     */
    long countByUserIdAndIsFeaturedTrue(Long userId);

    /**
     * Count testimonials by user ID and rating
     */
    @Query("SELECT COUNT(t) FROM Testimonial t WHERE t.user.id = :userId AND t.rating = :rating")
    long countByUserIdAndRating(@Param("userId") Long userId, @Param("rating") Integer rating);

    /**
     * Count approved testimonials by user ID and rating
     */
    @Query("SELECT COUNT(t) FROM Testimonial t WHERE t.user.id = :userId AND t.rating = :rating AND t.isApproved = true")
    long countByUserIdAndRatingAndIsApprovedTrue(@Param("userId") Long userId, @Param("rating") Integer rating);

    /**
     * Find testimonials by author name (case-insensitive search) for a specific
     * user
     */
    @Query("SELECT t FROM Testimonial t WHERE t.user.id = :userId AND LOWER(t.authorName) LIKE LOWER(CONCAT('%', :authorName, '%')) ORDER BY t.displayOrder ASC")
    List<Testimonial> findByUserIdAndAuthorNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("authorName") String authorName);
}
