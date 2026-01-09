package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.BulkDeleteRequest;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import com.hafizbahtiar.spring.common.security.SecurityService;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.PlatformType;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectStatus;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectType;
import com.hafizbahtiar.spring.features.portfolio.service.ProjectService;
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
 * REST controller for project management endpoints.
 * Handles project CRUD operations, featured projects, and search functionality.
 */
@RestController
@RequestMapping("/api/v1/portfolio/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
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
     * Create a new project
     * POST /api/v1/portfolio/projects
     * Requires: OWNER/ADMIN role OR portfolio.projects page WRITE permission
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'WRITE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectRequest request) {
        Long userId = getCurrentUserId();
        log.info("Project creation request received for user ID: {}, title: {}", userId, request.getTitle());
        ProjectResponse response = projectService.createProject(userId, request);
        return ResponseUtils.created(response, "Project created successfully");
    }

    /**
     * Get all projects for current user
     * GET /api/v1/portfolio/projects
     * Requires: OWNER/ADMIN role OR portfolio.projects page READ permission
     * Supports pagination via Pageable parameter (page, size, sort)
     * Query params: status, type, platform, featured, startDate, endDate, page,
     * size, sort
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'READ')")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProjectResponse>>> getUserProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectType type,
            @RequestParam(required = false) PlatformType platform,
            @RequestParam(required = false, defaultValue = "false") Boolean featured,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "displayOrder") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug(
                "Fetching projects for user ID: {}, status: {}, type: {}, platform: {}, featured: {}, page: {}, size: {}",
                userId, status, type, platform, featured, pageable.getPageNumber(), pageable.getPageSize());

        // If specific filters are provided, return non-paginated list (for backward
        // compatibility)
        // Otherwise, return paginated results
        if (featured) {
            List<ProjectResponse> projects = projectService.getFeaturedProjects(userId);
            Page<ProjectResponse> page = new PageImpl<>(projects, pageable, projects.size());
            return ResponseUtils.okPage(page);
        } else if (status != null && type != null) {
            List<ProjectResponse> projects = projectService.getUserProjectsByType(userId, type);
            // Filter by status if needed
            projects = projects.stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
            Page<ProjectResponse> page = new PageImpl<>(projects, pageable, projects.size());
            return ResponseUtils.okPage(page);
        } else if (status != null) {
            List<ProjectResponse> projects = projectService.getUserProjectsByStatus(userId, status);
            Page<ProjectResponse> page = new PageImpl<>(projects, pageable, projects.size());
            return ResponseUtils.okPage(page);
        } else if (type != null) {
            List<ProjectResponse> projects = projectService.getUserProjectsByType(userId, type);
            Page<ProjectResponse> page = new PageImpl<>(projects, pageable, projects.size());
            return ResponseUtils.okPage(page);
        } else if (platform != null) {
            List<ProjectResponse> projects = projectService.getUserProjectsByPlatform(userId, platform);
            Page<ProjectResponse> page = new PageImpl<>(projects, pageable, projects.size());
            return ResponseUtils.okPage(page);
        } else if (startDate != null && endDate != null) {
            List<ProjectResponse> projects = projectService.getUserProjectsByDateRange(userId, startDate, endDate);
            Page<ProjectResponse> page = new PageImpl<>(projects, pageable, projects.size());
            return ResponseUtils.okPage(page);
        } else {
            // Use paginated method when no specific filters are provided
            Page<ProjectResponse> projects = projectService.getUserProjects(userId, pageable);
            return ResponseUtils.okPage(projects);
        }
    }

    /**
     * Get project by ID
     * GET /api/v1/portfolio/projects/{id}
     * Requires: OWNER/ADMIN role OR portfolio.projects page READ permission
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'READ')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.debug("Fetching project ID: {} for user ID: {}", id, userId);

        ProjectResponse project = projectService.getProject(id, userId);

        // Verify ownership
        if (!project.getUserId().equals(userId) && !securityService.isAdmin()) {
            throw new SecurityException("You can only view your own projects");
        }

        return ResponseUtils.ok(project);
    }

    /**
     * Update project
     * PUT /api/v1/portfolio/projects/{id}
     * Requires: OWNER/ADMIN role OR portfolio.projects page WRITE permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'WRITE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        Long userId = getCurrentUserId();
        log.info("Project update request received for project ID: {}, user ID: {}", id, userId);
        ProjectResponse response = projectService.updateProject(id, userId, request);
        return ResponseUtils.ok(response, "Project updated successfully");
    }

    /**
     * Delete project
     * DELETE /api/v1/portfolio/projects/{id}
     * Requires: OWNER/ADMIN role OR portfolio.projects page DELETE permission
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("Project deletion request received for project ID: {}, user ID: {}", id, userId);
        projectService.deleteProject(id, userId);
        return ResponseUtils.noContent();
    }

    /**
     * Set/unset project as featured
     * PUT /api/v1/portfolio/projects/{id}/feature
     * Requires: OWNER/ADMIN role OR portfolio.projects page WRITE permission
     */
    @PutMapping("/{id}/feature")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'WRITE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> setFeatured(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean featured) {
        Long userId = getCurrentUserId();
        log.info("Set featured request received for project ID: {}, featured: {}, user ID: {}", id, featured, userId);
        ProjectResponse response = projectService.setFeatured(id, userId, featured);
        return ResponseUtils.ok(response, featured ? "Project set as featured" : "Project unset as featured");
    }

    /**
     * Bulk delete projects
     * POST /api/v1/portfolio/projects/bulk-delete
     * Requires: OWNER/ADMIN role OR portfolio.projects page DELETE permission
     * Body: BulkDeleteRequest with list of project IDs
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'DELETE')")
    public ResponseEntity<ApiResponse<BulkDeleteResponse>> bulkDeleteProjects(
            @Valid @RequestBody BulkDeleteRequest request) {
        Long userId = getCurrentUserId();
        log.info("Bulk delete projects request received for user ID: {}, IDs: {}", userId, request.getIds());
        BulkDeleteResponse response = projectService.bulkDeleteProjects(userId, request.getIds());
        return ResponseUtils.ok(response, "Bulk delete completed");
    }

    /**
     * Reorder projects
     * PUT /api/v1/portfolio/projects/reorder
     * Requires: OWNER/ADMIN role OR portfolio.projects page WRITE permission
     */
    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'WRITE')")
    public ResponseEntity<ApiResponse<Void>> reorderProjects(@RequestBody Map<Long, Integer> projectOrderMap) {
        Long userId = getCurrentUserId();
        log.info("Project reorder request received for user ID: {}", userId);
        projectService.reorderProjects(userId, projectOrderMap);
        return ResponseUtils.ok(null, "Projects reordered successfully");
    }

    /**
     * Search projects by title or description
     * GET /api/v1/portfolio/projects/search
     * Requires: OWNER/ADMIN role OR portfolio.projects page READ permission
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') or @securityUtils.hasPermission('PAGE', 'portfolio', 'portfolio.projects', 'READ')")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProjectResponse>>> searchProjects(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable) {
        Long userId = getCurrentUserId();
        log.debug("Searching projects for user ID: {} with query: {}", userId, query);
        Page<ProjectResponse> projects = projectService.searchProjects(userId, query, pageable);
        return ResponseUtils.okPage(projects);
    }
}
