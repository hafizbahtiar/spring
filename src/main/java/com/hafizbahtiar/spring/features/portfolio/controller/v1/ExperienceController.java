package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.EmploymentType;
import com.hafizbahtiar.spring.features.portfolio.service.ExperienceService;
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
 * REST controller for experience management endpoints.
 * Handles experience CRUD operations and display order management.
 */
@RestController
@RequestMapping("/api/v1/portfolio/experiences")
@RequiredArgsConstructor
@Slf4j
public class ExperienceController {

    private final ExperienceService experienceService;
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
     * Create a new experience
     * POST /api/v1/portfolio/experiences
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExperienceResponse>> createExperience(
            @Valid @RequestBody ExperienceRequest request) {
        Long userId = getCurrentUserId();
        log.info("Experience creation request received for user ID: {}, company: {}", userId, request.getCompany());
        ExperienceResponse response = experienceService.createExperience(userId, request);
        return ResponseUtils.created(response, "Experience created successfully");
    }

    /**
     * Get all experiences for current user
     * GET /api/v1/portfolio/experiences
     * Requires: Authenticated user
     * Supports pagination via Pageable parameter (page, size, sort)
     * Query params: current, employmentType, startDate, endDate, page, size, sort
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<ExperienceResponse>>> getUserExperiences(
            @RequestParam(required = false) Boolean current,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "displayOrder") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Fetching experiences for user ID: {}, current: {}, employmentType: {}, page: {}, size: {}",
                userId, current, employmentType, pageable.getPageNumber(), pageable.getPageSize());

        // If specific filters are provided, return non-paginated list (for backward
        // compatibility)
        // Otherwise, return paginated results
        if (current != null && current) {
            List<ExperienceResponse> experiences = experienceService.getCurrentExperiences(userId);
            Page<ExperienceResponse> page = new PageImpl<>(experiences, pageable, experiences.size());
            return ResponseUtils.okPage(page);
        } else if (current != null && !current) {
            List<ExperienceResponse> experiences = experienceService.getPastExperiences(userId);
            Page<ExperienceResponse> page = new PageImpl<>(experiences, pageable, experiences.size());
            return ResponseUtils.okPage(page);
        } else if (employmentType != null) {
            List<ExperienceResponse> experiences = experienceService.getUserExperiencesByType(userId, employmentType);
            Page<ExperienceResponse> page = new PageImpl<>(experiences, pageable, experiences.size());
            return ResponseUtils.okPage(page);
        } else if (startDate != null && endDate != null) {
            List<ExperienceResponse> experiences = experienceService.getUserExperiencesByDateRange(userId, startDate,
                    endDate);
            Page<ExperienceResponse> page = new PageImpl<>(experiences, pageable, experiences.size());
            return ResponseUtils.okPage(page);
        } else {
            // Use paginated method when no specific filters are provided
            Page<ExperienceResponse> experiences = experienceService.getUserExperiences(userId, pageable);
            return ResponseUtils.okPage(experiences);
        }
    }

    /**
     * Get experience by ID
     * GET /api/v1/portfolio/experiences/{id}
     * Requires: User owns the experience OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExperienceResponse>> getExperience(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching experience ID: {} for user ID: {}", id, userId);

        ExperienceResponse experience = experienceService.getExperience(id, userId);

        // Verify ownership
        if (!experience.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own experiences");
        }

        return ResponseUtils.ok(experience);
    }

    /**
     * Update experience
     * PUT /api/v1/portfolio/experiences/{id}
     * Requires: User owns the experience OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExperienceResponse>> updateExperience(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceRequest request) {
        Long userId = getCurrentUserId();
        log.info("Experience update request received for experience ID: {}, user ID: {}", id, userId);
        ExperienceResponse response = experienceService.updateExperience(id, userId, request);
        return ResponseUtils.ok(response, "Experience updated successfully");
    }

    /**
     * Delete experience
     * DELETE /api/v1/portfolio/experiences/{id}
     * Requires: User owns the experience OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Experience deletion request received for experience ID: {}, user ID: {}", id, userId);
        experienceService.deleteExperience(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Bulk delete experiences
     * POST /api/v1/portfolio/experiences/bulk-delete
     * Requires: Authenticated user
     * Body: BulkDeleteRequest with list of experience IDs
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDeleteExperiences(
            @Valid @RequestBody BulkDeleteRequest request) {
        Long userId = getCurrentUserId();
        log.info("Bulk delete experiences request received for user ID: {}, IDs: {}", userId, request.getIds());
        BulkDeleteResponse response = experienceService.bulkDeleteExperiences(userId, request.getIds());
        return ResponseUtils.ok(response, "Bulk delete completed");
    }

    /**
     * Reorder experiences
     * PUT /api/v1/portfolio/experiences/reorder
     * Requires: Authenticated user
     */
    @PutMapping("/reorder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderExperiences(@RequestBody Map<Long, Integer> experienceOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Experience reorder request received for user ID: {}", userId);
        experienceService.reorderExperiences(userId, experienceOrderMap);
        return ResponseUtils.ok(null, "Experiences reordered successfully");
    }
}
