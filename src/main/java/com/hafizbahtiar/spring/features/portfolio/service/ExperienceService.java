package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for experience management.
 * Handles CRUD operations and display order management for work experiences.
 */
public interface ExperienceService {

    /**
     * Create a new experience for a user.
     *
     * @param userId  User ID
     * @param request Experience creation request
     * @return Created ExperienceResponse
     */
    ExperienceResponse createExperience(Long userId, ExperienceRequest request);

    /**
     * Update an existing experience.
     *
     * @param experienceId Experience ID
     * @param userId       User ID (for ownership validation)
     * @param request      Update request
     * @return Updated ExperienceResponse
     */
    ExperienceResponse updateExperience(Long experienceId, Long userId, ExperienceRequest request);

    /**
     * Delete an experience.
     *
     * @param experienceId Experience ID
     * @param userId       User ID (for ownership validation)
     */
    void deleteExperience(Long experienceId, Long userId);

    /**
     * Bulk delete experiences.
     *
     * @param userId User ID (for ownership validation)
     * @param ids    List of experience IDs to delete
     * @return BulkDeleteResponse with deleted count and failed IDs
     */
    BulkDeleteResponse bulkDeleteExperiences(Long userId, List<Long> ids);

    /**
     * Get experience by ID.
     *
     * @param experienceId Experience ID
     * @param userId       User ID (for ownership validation)
     * @return ExperienceResponse
     */
    ExperienceResponse getExperience(Long experienceId, Long userId);

    /**
     * Get all experiences for a user.
     *
     * @param userId User ID
     * @return List of ExperienceResponse
     */
    List<ExperienceResponse> getUserExperiences(Long userId);

    /**
     * Get all experiences for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of ExperienceResponse
     */
    Page<ExperienceResponse> getUserExperiences(Long userId, Pageable pageable);

    /**
     * Get current (active) experiences for a user.
     *
     * @param userId User ID
     * @return List of ExperienceResponse
     */
    List<ExperienceResponse> getCurrentExperiences(Long userId);

    /**
     * Get past (completed) experiences for a user.
     *
     * @param userId User ID
     * @return List of ExperienceResponse
     */
    List<ExperienceResponse> getPastExperiences(Long userId);

    /**
     * Get experiences filtered by employment type.
     *
     * @param userId         User ID
     * @param employmentType Employment type (optional)
     * @return List of ExperienceResponse
     */
    List<ExperienceResponse> getUserExperiencesByType(Long userId, EmploymentType employmentType);

    /**
     * Get experiences within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of ExperienceResponse
     */
    List<ExperienceResponse> getUserExperiencesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Reorder experiences by updating display order.
     *
     * @param userId             User ID
     * @param experienceOrderMap Map of experience ID to display order
     */
    void reorderExperiences(Long userId, Map<Long, Integer> experienceOrderMap);
}
