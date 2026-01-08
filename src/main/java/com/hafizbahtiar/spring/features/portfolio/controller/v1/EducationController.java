package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.DegreeType;
import com.hafizbahtiar.spring.features.portfolio.service.EducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for education management endpoints.
 * Handles education CRUD operations and display order management.
 */
@RestController
@RequestMapping("/api/v1/portfolio/educations")
@RequiredArgsConstructor
@Slf4j
public class EducationController {

    private final EducationService educationService;
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
     * Create a new education
     * POST /api/v1/portfolio/educations
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EducationResponse>> createEducation(
            @Valid @RequestBody EducationRequest request) {
        Long userId = getCurrentUserId();
        log.info("Education creation request received for user ID: {}, institution: {}", userId,
                request.getInstitution());
        EducationResponse response = educationService.createEducation(userId, request);
        return ResponseUtils.created(response, "Education created successfully");
    }

    /**
     * Get all educations for current user
     * GET /api/v1/portfolio/educations
     * Requires: Authenticated user
     * Supports pagination via Pageable parameter (page, size, sort)
     * Query params: current, degree, startDate, endDate, page, size, sort
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<EducationResponse>>> getUserEducations(
            @RequestParam(required = false) Boolean current,
            @RequestParam(required = false) DegreeType degree,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "displayOrder") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Fetching educations for user ID: {}, current: {}, degree: {}, page: {}, size: {}",
                userId, current, degree, pageable.getPageNumber(), pageable.getPageSize());

        // If specific filters are provided, return non-paginated list (for backward
        // compatibility)
        // Otherwise, return paginated results
        if (current != null && current) {
            List<EducationResponse> educations = educationService.getCurrentEducations(userId);
            // Convert to Page for consistent response format
            Page<EducationResponse> page = new PageImpl<>(educations, pageable, educations.size());
            return ResponseUtils.okPage(page);
        } else if (current != null && !current) {
            List<EducationResponse> educations = educationService.getCompletedEducations(userId);
            Page<EducationResponse> page = new PageImpl<>(educations, pageable, educations.size());
            return ResponseUtils.okPage(page);
        } else if (degree != null) {
            List<EducationResponse> educations = educationService.getUserEducationsByDegree(userId, degree);
            Page<EducationResponse> page = new PageImpl<>(educations, pageable, educations.size());
            return ResponseUtils.okPage(page);
        } else if (startDate != null && endDate != null) {
            List<EducationResponse> educations = educationService.getUserEducationsByDateRange(userId, startDate,
                    endDate);
            Page<EducationResponse> page = new PageImpl<>(educations, pageable, educations.size());
            return ResponseUtils.okPage(page);
        } else {
            // Use paginated method when no specific filters are provided
            Page<EducationResponse> educations = educationService.getUserEducations(userId, pageable);
            return ResponseUtils.okPage(educations);
        }
    }

    /**
     * Get education by ID
     * GET /api/v1/portfolio/educations/{id}
     * Requires: User owns the education OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EducationResponse>> getEducation(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching education ID: {} for user ID: {}", id, userId);

        EducationResponse education = educationService.getEducation(id, userId);

        // Verify ownership
        if (!education.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own educations");
        }

        return ResponseUtils.ok(education);
    }

    /**
     * Update education
     * PUT /api/v1/portfolio/educations/{id}
     * Requires: User owns the education OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EducationResponse>> updateEducation(
            @PathVariable Long id,
            @Valid @RequestBody EducationRequest request) {
        Long userId = getCurrentUserId();
        log.info("Education update request received for education ID: {}, user ID: {}", id, userId);
        EducationResponse response = educationService.updateEducation(id, userId, request);
        return ResponseUtils.ok(response, "Education updated successfully");
    }

    /**
     * Delete education
     * DELETE /api/v1/portfolio/educations/{id}
     * Requires: User owns the education OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Education deletion request received for education ID: {}, user ID: {}", id, userId);
        educationService.deleteEducation(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Bulk delete educations
     * POST /api/v1/portfolio/educations/bulk-delete
     * Requires: Authenticated user
     * Body: BulkDeleteRequest with list of education IDs
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDeleteEducations(
            @Valid @RequestBody BulkDeleteRequest request) {
        Long userId = getCurrentUserId();
        log.info("Bulk delete educations request received for user ID: {}, IDs: {}", userId, request.getIds());
        BulkDeleteResponse response = educationService.bulkDeleteEducations(userId, request.getIds());
        return ResponseUtils.ok(response, "Bulk delete completed");
    }

    /**
     * Reorder educations
     * PUT /api/v1/portfolio/educations/reorder
     * Requires: Authenticated user
     */
    @PutMapping("/reorder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderEducations(@RequestBody Map<Long, Integer> educationOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Education reorder request received for user ID: {}", userId);
        educationService.reorderEducations(userId, educationOrderMap);
        return ResponseUtils.ok(null, "Educations reordered successfully");
    }
}
