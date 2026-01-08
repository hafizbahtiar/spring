package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialResponse;
import com.hafizbahtiar.spring.features.portfolio.service.TestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for testimonial management endpoints.
 * Handles testimonial CRUD operations, approval workflow, featured status,
 * filtering, and display order management.
 */
@RestController
@RequestMapping("/api/v1/portfolio/testimonials")
@RequiredArgsConstructor
@Slf4j
public class TestimonialController {

    private final TestimonialService testimonialService;
    private final SecurityService securityService;

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * Create a new testimonial
     * POST /api/v1/portfolio/testimonials
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TestimonialResponse>> createTestimonial(
            @Valid @RequestBody TestimonialRequest request) {
        Long userId = getCurrentUserId();
        log.info("Testimonial creation request received for user ID: {}, author: {}", userId, request.getAuthorName());
        TestimonialResponse response = testimonialService.createTestimonial(userId, request);
        return ResponseUtils.created(response, "Testimonial created successfully");
    }

    /**
     * Get all testimonials for current user
     * GET /api/v1/portfolio/testimonials
     * Requires: Authenticated user
     * Supports pagination via Pageable parameter (page, size, sort)
     * Query params: rating, featured, projectId, page, size, sort
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<TestimonialResponse>>> getUserTestimonials(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "false") Boolean featured,
            @RequestParam(required = false) Long projectId,
            @PageableDefault(size = 10, sort = "displayOrder") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Fetching testimonials for user ID: {}, rating: {}, featured: {}, projectId: {}, page: {}, size: {}",
                userId, rating, featured, projectId, pageable.getPageNumber(), pageable.getPageSize());

        // If specific filters are provided, return non-paginated list (for backward
        // compatibility)
        // Otherwise, return paginated results
        if (featured) {
            List<TestimonialResponse> testimonials = testimonialService.getFeaturedTestimonials(userId);
            // Apply additional filters if provided
            if (rating != null) {
                testimonials = testimonials.stream()
                        .filter(t -> rating.equals(t.getRating()))
                        .toList();
            }
            if (projectId != null) {
                testimonials = testimonials.stream()
                        .filter(t -> projectId.equals(t.getProjectId()))
                        .toList();
            }
            Page<TestimonialResponse> page = new PageImpl<>(testimonials, pageable, testimonials.size());
            return ResponseUtils.okPage(page);
        } else if (rating != null) {
            List<TestimonialResponse> testimonials = testimonialService.getUserTestimonialsByRating(userId, rating);
            // Filter by project if provided
            if (projectId != null) {
                testimonials = testimonials.stream()
                        .filter(t -> projectId.equals(t.getProjectId()))
                        .toList();
            }
            Page<TestimonialResponse> page = new PageImpl<>(testimonials, pageable, testimonials.size());
            return ResponseUtils.okPage(page);
        } else if (projectId != null) {
            List<TestimonialResponse> testimonials = testimonialService.getUserTestimonialsByProject(userId, projectId);
            Page<TestimonialResponse> page = new PageImpl<>(testimonials, pageable, testimonials.size());
            return ResponseUtils.okPage(page);
        } else {
            // Use paginated method when no specific filters are provided
            Page<TestimonialResponse> testimonials = testimonialService.getUserTestimonials(userId, pageable);
            return ResponseUtils.okPage(testimonials);
        }
    }

    /**
     * Get approved testimonials (public endpoint)
     * GET /api/v1/portfolio/testimonials/public
     * Requires: No authentication (public access)
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<TestimonialResponse>>> getPublicTestimonials(
            @RequestParam(required = false) Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required for public testimonials");
        }
        log.debug("Fetching public testimonials for user ID: {}", userId);
        List<TestimonialResponse> testimonials = testimonialService.getApprovedTestimonials(userId);
        return ResponseUtils.ok(testimonials);
    }

    /**
     * Get testimonial by ID
     * GET /api/v1/portfolio/testimonials/{id}
     * Requires: User owns the testimonial OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TestimonialResponse>> getTestimonial(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching testimonial ID: {} for user ID: {}", id, userId);

        TestimonialResponse testimonial = testimonialService.getTestimonial(id, userId);

        // Verify ownership (service already validates, but double-check for security)
        if (!testimonial.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own testimonials");
        }

        return ResponseUtils.ok(testimonial);
    }

    /**
     * Update testimonial
     * PUT /api/v1/portfolio/testimonials/{id}
     * Requires: User owns the testimonial OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TestimonialResponse>> updateTestimonial(
            @PathVariable Long id,
            @Valid @RequestBody TestimonialRequest request) {
        Long userId = getCurrentUserId();
        log.info("Testimonial update request received for testimonial ID: {}, user ID: {}", id, userId);
        TestimonialResponse response = testimonialService.updateTestimonial(id, userId, request);
        return ResponseUtils.ok(response, "Testimonial updated successfully");
    }

    /**
     * Delete testimonial
     * DELETE /api/v1/portfolio/testimonials/{id}
     * Requires: User owns the testimonial OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteTestimonial(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Testimonial deletion request received for testimonial ID: {}, user ID: {}", id, userId);
        testimonialService.deleteTestimonial(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Set/unset testimonial as featured
     * PUT /api/v1/portfolio/testimonials/{id}/feature?featured=true|false
     * Requires: User owns the testimonial OR ADMIN role
     */
    @PutMapping("/{id}/feature")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TestimonialResponse>> setFeatured(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean featured) {
        Long userId = getCurrentUserId();
        log.info("Set featured request received for testimonial ID: {}, featured: {}, user ID: {}", id, featured,
                userId);
        TestimonialResponse response = testimonialService.setFeatured(id, userId, featured);
        return ResponseUtils.ok(response, featured ? "Testimonial set as featured" : "Testimonial unset as featured");
    }

    /**
     * Approve testimonial
     * PUT /api/v1/portfolio/testimonials/{id}/approve
     * Requires: User owns the testimonial OR ADMIN role
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TestimonialResponse>> approveTestimonial(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Approve testimonial request received for testimonial ID: {}, user ID: {}", id, userId);
        TestimonialResponse response = testimonialService.approveTestimonial(id, userId);
        return ResponseUtils.ok(response, "Testimonial approved successfully");
    }

    /**
     * Bulk delete testimonials
     * POST /api/v1/portfolio/testimonials/bulk-delete
     * Requires: Authenticated user
     * Body: BulkDeleteRequest with list of testimonial IDs
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDeleteTestimonials(
            @Valid @RequestBody BulkDeleteRequest request) {
        Long userId = getCurrentUserId();
        log.info("Bulk delete testimonials request received for user ID: {}, IDs: {}", userId, request.getIds());
        BulkDeleteResponse response = testimonialService.bulkDeleteTestimonials(userId, request.getIds());
        return ResponseUtils.ok(response, "Bulk delete completed");
    }

    /**
     * Reorder testimonials
     * PUT /api/v1/portfolio/testimonials/reorder
     * Requires: Authenticated user
     */
    @PutMapping("/reorder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderTestimonials(
            @RequestBody Map<Long, Integer> testimonialOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Testimonial reorder request received for user ID: {}", userId);
        testimonialService.reorderTestimonials(userId, testimonialOrderMap);
        return ResponseUtils.ok(null, "Testimonials reordered successfully");
    }
}
