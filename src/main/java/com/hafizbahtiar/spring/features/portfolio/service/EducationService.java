package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.DegreeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for education management.
 * Handles CRUD operations and display order management for education history.
 */
public interface EducationService {

    /**
     * Create a new education for a user.
     *
     * @param userId  User ID
     * @param request Education creation request
     * @return Created EducationResponse
     */
    EducationResponse createEducation(Long userId, EducationRequest request);

    /**
     * Update an existing education.
     *
     * @param educationId Education ID
     * @param userId     User ID (for ownership validation)
     * @param request    Update request
     * @return Updated EducationResponse
     */
    EducationResponse updateEducation(Long educationId, Long userId, EducationRequest request);

    /**
     * Delete an education.
     *
     * @param educationId Education ID
     * @param userId     User ID (for ownership validation)
     */
    void deleteEducation(Long educationId, Long userId);

    /**
     * Bulk delete educations.
     *
     * @param userId User ID (for ownership validation)
     * @param ids    List of education IDs to delete
     * @return BulkDeleteResponse with deleted count and failed IDs
     */
    BulkDeleteResponse bulkDeleteEducations(Long userId, List<Long> ids);

    /**
     * Get education by ID.
     *
     * @param educationId Education ID
     * @param userId     User ID (for ownership validation)
     * @return EducationResponse
     */
    EducationResponse getEducation(Long educationId, Long userId);

    /**
     * Get all educations for a user.
     *
     * @param userId User ID
     * @return List of EducationResponse
     */
    List<EducationResponse> getUserEducations(Long userId);

    /**
     * Get all educations for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of EducationResponse
     */
    Page<EducationResponse> getUserEducations(Long userId, Pageable pageable);

    /**
     * Get current (ongoing) educations for a user.
     *
     * @param userId User ID
     * @return List of EducationResponse
     */
    List<EducationResponse> getCurrentEducations(Long userId);

    /**
     * Get completed educations for a user.
     *
     * @param userId User ID
     * @return List of EducationResponse
     */
    List<EducationResponse> getCompletedEducations(Long userId);

    /**
     * Get educations filtered by degree type.
     *
     * @param userId User ID
     * @param degree Degree type (optional)
     * @return List of EducationResponse
     */
    List<EducationResponse> getUserEducationsByDegree(Long userId, DegreeType degree);

    /**
     * Get educations within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of EducationResponse
     */
    List<EducationResponse> getUserEducationsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Reorder educations by updating display order.
     *
     * @param userId           User ID
     * @param educationOrderMap Map of education ID to display order
     */
    void reorderEducations(Long userId, Map<Long, Integer> educationOrderMap);
}

