package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.PlatformType;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectStatus;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for project management.
 * Handles CRUD operations, featured projects, and search functionality.
 */
public interface ProjectService {

    /**
     * Create a new project for a user.
     *
     * @param userId  User ID
     * @param request Project creation request
     * @return Created ProjectResponse
     */
    ProjectResponse createProject(Long userId, ProjectRequest request);

    /**
     * Update an existing project.
     *
     * @param projectId Project ID
     * @param userId    User ID (for ownership validation)
     * @param request   Update request
     * @return Updated ProjectResponse
     */
    ProjectResponse updateProject(Long projectId, Long userId, ProjectRequest request);

    /**
     * Delete a project.
     *
     * @param projectId Project ID
     * @param userId    User ID (for ownership validation)
     */
    void deleteProject(Long projectId, Long userId);

    /**
     * Bulk delete projects.
     *
     * @param userId User ID (for ownership validation)
     * @param ids    List of project IDs to delete
     * @return BulkDeleteResponse with deleted count and failed IDs
     */
    BulkDeleteResponse bulkDeleteProjects(Long userId, List<Long> ids);

    /**
     * Get project by ID.
     *
     * @param projectId Project ID
     * @param userId    User ID (for ownership validation)
     * @return ProjectResponse
     */
    ProjectResponse getProject(Long projectId, Long userId);

    /**
     * Get all projects for a user.
     *
     * @param userId User ID
     * @return List of ProjectResponse
     */
    List<ProjectResponse> getUserProjects(Long userId);

    /**
     * Get all projects for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of ProjectResponse
     */
    Page<ProjectResponse> getUserProjects(Long userId, Pageable pageable);

    /**
     * Get projects filtered by status.
     *
     * @param userId User ID
     * @param status Project status (optional)
     * @return List of ProjectResponse
     */
    List<ProjectResponse> getUserProjectsByStatus(Long userId, ProjectStatus status);

    /**
     * Get projects filtered by type.
     *
     * @param userId User ID
     * @param type   Project type (optional)
     * @return List of ProjectResponse
     */
    List<ProjectResponse> getUserProjectsByType(Long userId, ProjectType type);

    /**
     * Get projects filtered by platform.
     *
     * @param userId   User ID
     * @param platform Project platform (optional)
     * @return List of ProjectResponse
     */
    List<ProjectResponse> getUserProjectsByPlatform(Long userId, PlatformType platform);

    /**
     * Get featured projects for a user.
     *
     * @param userId User ID
     * @return List of ProjectResponse
     */
    List<ProjectResponse> getFeaturedProjects(Long userId);

    /**
     * Set project as featured or unfeatured.
     *
     * @param projectId Project ID
     * @param userId    User ID (for ownership validation)
     * @param featured  Whether to set as featured
     * @return Updated ProjectResponse
     */
    ProjectResponse setFeatured(Long projectId, Long userId, boolean featured);

    /**
     * Search projects by title or description.
     *
     * @param userId     User ID
     * @param searchTerm Search term
     * @return List of ProjectResponse
     */
    List<ProjectResponse> searchProjects(Long userId, String searchTerm);

    /**
     * Search projects by title or description (paginated).
     *
     * @param userId     User ID
     * @param searchTerm Search term
     * @param pageable   Pagination parameters
     * @return Page of ProjectResponse
     */
    Page<ProjectResponse> searchProjects(Long userId, String searchTerm, Pageable pageable);

    /**
     * Get projects within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of ProjectResponse
     */
    List<ProjectResponse> getUserProjectsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Reorder projects by updating display order.
     *
     * @param userId          User ID
     * @param projectOrderMap Map of project ID to display order
     */
    void reorderProjects(Long userId, Map<Long, Integer> projectOrderMap);
}
