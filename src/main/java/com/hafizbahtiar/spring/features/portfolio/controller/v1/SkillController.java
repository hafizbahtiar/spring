package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.ProficiencyLevel;
import com.hafizbahtiar.spring.features.portfolio.entity.SkillCategory;
import com.hafizbahtiar.spring.features.portfolio.service.SkillService;
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
 * REST controller for skill management endpoints.
 * Handles skill CRUD operations and display order management.
 */
@RestController
@RequestMapping("/api/v1/portfolio/skills")
@RequiredArgsConstructor
@Slf4j
public class SkillController {

    private final SkillService skillService;
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
     * Create a new skill
     * POST /api/v1/portfolio/skills
     * Requires: Authenticated user
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(@Valid @RequestBody SkillRequest request) {
        Long userId = getCurrentUserId();
        log.info("Skill creation request received for user ID: {}, skill name: {}", userId, request.getName());
        SkillResponse response = skillService.createSkill(userId, request);
        return ResponseUtils.created(response, "Skill created successfully");
    }

    /**
     * Get all skills for current user
     * GET /api/v1/portfolio/skills
     * Requires: Authenticated user
     * Supports optional pagination via Pageable parameter (page, size, sort)
     * If pagination params are provided, returns Page; otherwise returns List (for backward compatibility)
     * Query params: category, proficiency, activeOnly, page, size, sort
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserSkills(
            @RequestParam(required = false) SkillCategory category,
            @RequestParam(required = false) ProficiencyLevel proficiency,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @PageableDefault(size = Integer.MAX_VALUE, sort = "displayOrder") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Fetching skills for user ID: {}, category: {}, proficiency: {}, activeOnly: {}, page: {}, size: {}",
                userId, category, proficiency, activeOnly, pageable.getPageNumber(), pageable.getPageSize());

        // Check if pagination is requested (default size is Integer.MAX_VALUE, so check if explicitly set)
        boolean usePagination = pageable.getPageSize() != Integer.MAX_VALUE;

        // If specific filters are provided, return non-paginated list (for backward compatibility)
        // Otherwise, return paginated or flat list based on pageable
        if (activeOnly) {
            List<SkillResponse> skills = skillService.getActiveUserSkills(userId);
            if (usePagination) {
                Page<SkillResponse> page = new PageImpl<>(skills, pageable, skills.size());
                return ResponseUtils.okPage(page);
            }
            return ResponseUtils.ok(skills);
        } else if (category != null && proficiency != null) {
            List<SkillResponse> skills = skillService.getUserSkillsByCategory(userId, category);
            // Filter by proficiency if needed (service doesn't have combined filter)
            if (proficiency != null) {
                skills = skills.stream()
                        .filter(s -> s.getProficiency() == proficiency)
                        .toList();
            }
            if (usePagination) {
                Page<SkillResponse> page = new PageImpl<>(skills, pageable, skills.size());
                return ResponseUtils.okPage(page);
            }
            return ResponseUtils.ok(skills);
        } else if (category != null) {
            List<SkillResponse> skills = skillService.getUserSkillsByCategory(userId, category);
            if (usePagination) {
                Page<SkillResponse> page = new PageImpl<>(skills, pageable, skills.size());
                return ResponseUtils.okPage(page);
            }
            return ResponseUtils.ok(skills);
        } else if (proficiency != null) {
            List<SkillResponse> skills = skillService.getUserSkillsByProficiency(userId, proficiency);
            if (usePagination) {
                Page<SkillResponse> page = new PageImpl<>(skills, pageable, skills.size());
                return ResponseUtils.okPage(page);
            }
            return ResponseUtils.ok(skills);
        } else {
            // Use paginated method when no specific filters and pagination is requested
            if (usePagination) {
                Page<SkillResponse> skills = skillService.getUserSkills(userId, pageable);
                return ResponseUtils.okPage(skills);
            }
            // Return flat list for backward compatibility
            List<SkillResponse> skills = skillService.getUserSkills(userId);
            return ResponseUtils.ok(skills);
        }
    }

    /**
     * Get skill by ID
     * GET /api/v1/portfolio/skills/{id}
     * Requires: User owns the skill OR ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SkillResponse>> getSkill(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching skill ID: {} for user ID: {}", id, userId);

        SkillResponse skill = skillService.getSkill(id, userId);

        // Verify ownership (service already validates, but double-check for security)
        if (!skill.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own skills");
        }

        return ResponseUtils.ok(skill);
    }

    /**
     * Update skill
     * PUT /api/v1/portfolio/skills/{id}
     * Requires: User owns the skill OR ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SkillResponse>> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        Long userId = getCurrentUserId();
        log.info("Skill update request received for skill ID: {}, user ID: {}", id, userId);
        SkillResponse response = skillService.updateSkill(id, userId, request);
        return ResponseUtils.ok(response, "Skill updated successfully");
    }

    /**
     * Delete skill
     * DELETE /api/v1/portfolio/skills/{id}
     * Requires: User owns the skill OR ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Skill deletion request received for skill ID: {}, user ID: {}", id, userId);
        skillService.deleteSkill(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Bulk delete skills
     * POST /api/v1/portfolio/skills/bulk-delete
     * Requires: Authenticated user
     * Body: BulkDeleteRequest with list of skill IDs
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDeleteSkills(
            @Valid @RequestBody BulkDeleteRequest request) {
        Long userId = getCurrentUserId();
        log.info("Bulk delete skills request received for user ID: {}, IDs: {}", userId, request.getIds());
        BulkDeleteResponse response = skillService.bulkDeleteSkills(userId, request.getIds());
        return ResponseUtils.ok(response, "Bulk delete completed");
    }

    /**
     * Reorder skills
     * PUT /api/v1/portfolio/skills/reorder
     * Requires: Authenticated user
     */
    @PutMapping("/reorder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderSkills(@RequestBody Map<Long, Integer> skillOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Skill reorder request received for user ID: {}", userId);
        skillService.reorderSkills(userId, skillOrderMap);
        return ResponseUtils.ok(null, "Skills reordered successfully");
    }
}
