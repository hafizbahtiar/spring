package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.ProjectRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Project;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectStatus;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectType;
import com.hafizbahtiar.spring.features.portfolio.mapper.ProjectMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.ProjectRepository;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
import com.hafizbahtiar.spring.features.portfolio.exception.ProjectNotFoundException;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ProjectService.
 * Handles project CRUD operations, URL validation, featured projects, and
 * search functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final PortfolioLoggingService portfolioLoggingService;

    /**
     * Get current HttpServletRequest for logging context
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not retrieve current request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ProjectResponse createProject(Long userId, ProjectRequest request) {
        log.debug("Creating project for user ID: {}, title: {}", userId, request.getTitle());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate date range
        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw PortfolioException.invalidDateRange("End date must be after start date");
        }

        // Validate URLs if provided
        validateUrl(request.getGithubUrl(), "GitHub URL");
        validateUrl(request.getLiveUrl(), "Live URL");
        validateUrl(request.getImageUrl(), "Image URL");

        // Map request to entity
        Project project = projectMapper.toEntity(request);
        project.setUser(user);

        // Set display order if not provided
        if (project.getDisplayOrder() == null) {
            long maxOrder = projectRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Project::getDisplayOrder)
                    .max()
                    .orElse(-1);
            project.setDisplayOrder((int) (maxOrder + 1));
        }

        long startTime = System.currentTimeMillis();
        Project savedProject = projectRepository.save(project);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Project created successfully with ID: {} for user ID: {}", savedProject.getId(), userId);

        // Log project creation
        portfolioLoggingService.logProjectCreated(
                savedProject.getId(),
                userId,
                savedProject.getTitle(),
                getCurrentRequest(),
                responseTime);

        return projectMapper.toResponse(savedProject);
    }

    @Override
    public ProjectResponse updateProject(Long projectId, Long userId, ProjectRequest request) {
        log.debug("Updating project ID: {} for user ID: {}", projectId, userId);

        // Validate project exists and belongs to user
        Project project = projectRepository.findByUserIdAndId(userId, projectId)
                .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(projectId, userId));

        // Validate date range
        if (request.getEndDate() != null && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw PortfolioException.invalidDateRange("End date must be after start date");
        }

        // Validate URLs if provided
        validateUrl(request.getGithubUrl(), "GitHub URL");
        validateUrl(request.getLiveUrl(), "Live URL");
        validateUrl(request.getImageUrl(), "Image URL");

        // Update entity from request
        projectMapper.updateEntityFromRequest(request, project);

        long startTime = System.currentTimeMillis();
        Project updatedProject = projectRepository.save(project);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Project updated successfully with ID: {}", updatedProject.getId());

        // Log project update
        portfolioLoggingService.logProjectUpdated(
                updatedProject.getId(),
                userId,
                updatedProject.getTitle(),
                getCurrentRequest(),
                responseTime);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public void deleteProject(Long projectId, Long userId) {
        log.debug("Deleting project ID: {} for user ID: {}", projectId, userId);

        // Validate project exists and belongs to user
        Project project = projectRepository.findByUserIdAndId(userId, projectId)
                .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(projectId, userId));

        String projectTitle = project.getTitle();
        projectRepository.delete(project);
        log.info("Project deleted successfully with ID: {}", projectId);

        // Log project deletion
        portfolioLoggingService.logProjectDeleted(projectId, userId, projectTitle, getCurrentRequest());
    }

    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteProjects(Long userId, List<Long> ids) {
        log.debug("Bulk deleting projects for user ID: {}, IDs: {}", userId, ids);

        List<Long> failedIds = new ArrayList<>();
        int deletedCount = 0;

        for (Long projectId : ids) {
            try {
                // Validate project exists and belongs to user
                Project project = projectRepository.findByUserIdAndId(userId, projectId)
                        .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(projectId, userId));

                String projectTitle = project.getTitle();
                projectRepository.delete(project);
                deletedCount++;

                // Log individual project deletion
                portfolioLoggingService.logProjectDeleted(projectId, userId, projectTitle, getCurrentRequest());
            } catch (Exception e) {
                log.warn("Failed to delete project ID: {} for user ID: {} - {}", projectId, userId, e.getMessage());
                failedIds.add(projectId);
            }
        }

        log.info("Bulk delete completed for user ID: {} - Deleted: {}, Failed: {}", userId, deletedCount,
                failedIds.size());

        if (failedIds.isEmpty()) {
            return BulkDeleteResponse.success(deletedCount);
        } else {
            return BulkDeleteResponse.withFailures(deletedCount, failedIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId, Long userId) {
        log.debug("Fetching project ID: {} for user ID: {}", projectId, userId);

        Project project = projectRepository.findByUserIdAndId(userId, projectId)
                .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(projectId, userId));

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(Long userId) {
        log.debug("Fetching all projects for user ID: {}", userId);
        List<Project> projects = projectRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getUserProjects(Long userId, Pageable pageable) {
        log.debug("Fetching projects (paginated) for user ID: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Project> projects = projectRepository.findByUserIdOrderByDisplayOrderAsc(userId, pageable);
        return projects.map(projectMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjectsByStatus(Long userId, ProjectStatus status) {
        log.debug("Fetching projects for user ID: {} by status: {}", userId, status);
        List<Project> projects = status != null
                ? projectRepository.findByUserIdAndStatusOrderByDisplayOrderAsc(userId, status)
                : projectRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjectsByType(Long userId, ProjectType type) {
        log.debug("Fetching projects for user ID: {} by type: {}", userId, type);
        List<Project> projects = type != null
                ? projectRepository.findByUserIdAndTypeOrderByDisplayOrderAsc(userId, type)
                : projectRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getFeaturedProjects(Long userId) {
        log.debug("Fetching featured projects for user ID: {}", userId);
        List<Project> projects = projectRepository.findByUserIdAndIsFeaturedTrueOrderByDisplayOrderAsc(userId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public ProjectResponse setFeatured(Long projectId, Long userId, boolean featured) {
        log.debug("Setting project ID: {} featured status to: {} for user ID: {}", projectId, featured, userId);

        // Validate project exists and belongs to user
        Project project = projectRepository.findByUserIdAndId(userId, projectId)
                .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(projectId, userId));

        String projectTitle = project.getTitle();
        if (featured) {
            project.setFeatured();
        } else {
            project.unsetFeatured();
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project featured status updated successfully with ID: {}", updatedProject.getId());

        // Log project featured status change
        portfolioLoggingService.logProjectFeatured(
                updatedProject.getId(),
                userId,
                projectTitle,
                featured,
                getCurrentRequest());

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> searchProjects(Long userId, String searchTerm) {
        log.debug("Searching projects for user ID: {} with term: {}", userId, searchTerm);
        List<Project> projects = projectRepository.searchByUserIdAndTitleOrDescription(userId, searchTerm);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(Long userId, String searchTerm, Pageable pageable) {
        log.debug("Searching projects (paginated) for user ID: {} with term: {}", userId, searchTerm);
        Page<Project> projects = projectRepository.searchByUserIdAndTitleOrDescription(userId, searchTerm, pageable);
        return projects.map(projectMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjectsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching projects for user ID: {} in date range: {} to {}", userId, startDate, endDate);
        List<Project> projects = projectRepository.findByUserIdAndStartDateBetween(userId, startDate, endDate);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public void reorderProjects(Long userId, Map<Long, Integer> projectOrderMap) {
        log.debug("Reordering projects for user ID: {}", userId);

        projectOrderMap.forEach((projectId, displayOrder) -> {
            Project project = projectRepository.findByUserIdAndId(userId, projectId)
                    .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(projectId, userId));
            project.setDisplayOrder(displayOrder);
            projectRepository.save(project);
        });

        log.info("Projects reordered successfully for user ID: {}", userId);

        // Log projects reorder
        portfolioLoggingService.logProjectsReordered(userId, getCurrentRequest());
    }

    /**
     * Validate URL format.
     */
    private void validateUrl(String url, String fieldName) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw PortfolioException.invalidUrl(url, fieldName);
            }
        }
    }
}
