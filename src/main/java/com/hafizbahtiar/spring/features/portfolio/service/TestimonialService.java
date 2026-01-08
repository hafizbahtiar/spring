package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for testimonial management.
 * Handles CRUD operations, approval workflow, featured status, and display
 * order management for user testimonials.
 */
public interface TestimonialService {

    /**
     * Create a new testimonial for a user.
     *
     * @param userId  User ID
     * @param request Testimonial creation request
     * @return Created TestimonialResponse
     */
    TestimonialResponse createTestimonial(Long userId, TestimonialRequest request);

    /**
     * Update an existing testimonial.
     *
     * @param testimonialId Testimonial ID
     * @param userId        User ID (for ownership validation)
     * @param request       Update request
     * @return Updated TestimonialResponse
     */
    TestimonialResponse updateTestimonial(Long testimonialId, Long userId, TestimonialRequest request);

    /**
     * Delete a testimonial.
     *
     * @param testimonialId Testimonial ID
     * @param userId        User ID (for ownership validation)
     */
    void deleteTestimonial(Long testimonialId, Long userId);

    /**
     * Bulk delete testimonials.
     *
     * @param userId User ID (for ownership validation)
     * @param ids    List of testimonial IDs to delete
     * @return BulkDeleteResponse with deleted count and failed IDs
     */
    BulkDeleteResponse bulkDeleteTestimonials(Long userId, List<Long> ids);

    /**
     * Get testimonial by ID.
     *
     * @param testimonialId Testimonial ID
     * @param userId        User ID (for ownership validation)
     * @return TestimonialResponse
     */
    TestimonialResponse getTestimonial(Long testimonialId, Long userId);

    /**
     * Get all testimonials for a user.
     *
     * @param userId User ID
     * @return List of TestimonialResponse
     */
    List<TestimonialResponse> getUserTestimonials(Long userId);

    /**
     * Get all testimonials for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of TestimonialResponse
     */
    Page<TestimonialResponse> getUserTestimonials(Long userId, Pageable pageable);

    /**
     * Get approved testimonials for a user (for public display).
     *
     * @param userId User ID
     * @return List of TestimonialResponse
     */
    List<TestimonialResponse> getApprovedTestimonials(Long userId);

    /**
     * Get featured testimonials for a user.
     *
     * @param userId User ID
     * @return List of TestimonialResponse
     */
    List<TestimonialResponse> getFeaturedTestimonials(Long userId);

    /**
     * Get testimonials for a user filtered by rating.
     *
     * @param userId User ID
     * @param rating Rating (1-5)
     * @return List of TestimonialResponse
     */
    List<TestimonialResponse> getUserTestimonialsByRating(Long userId, Integer rating);

    /**
     * Get testimonials for a user filtered by project ID.
     *
     * @param userId    User ID
     * @param projectId Project ID (optional)
     * @return List of TestimonialResponse
     */
    List<TestimonialResponse> getUserTestimonialsByProject(Long userId, Long projectId);

    /**
     * Set/unset testimonial as featured.
     *
     * @param testimonialId Testimonial ID
     * @param userId        User ID (for ownership validation)
     * @param featured      Whether to set as featured
     * @return Updated TestimonialResponse
     */
    TestimonialResponse setFeatured(Long testimonialId, Long userId, Boolean featured);

    /**
     * Approve testimonial (admin or user can approve their own).
     *
     * @param testimonialId Testimonial ID
     * @param userId        User ID (for ownership validation)
     * @return Updated TestimonialResponse
     */
    TestimonialResponse approveTestimonial(Long testimonialId, Long userId);

    /**
     * Reorder testimonials by updating display order.
     *
     * @param userId              User ID
     * @param testimonialOrderMap Map of testimonial ID to display order
     */
    void reorderTestimonials(Long userId, Map<Long, Integer> testimonialOrderMap);
}
