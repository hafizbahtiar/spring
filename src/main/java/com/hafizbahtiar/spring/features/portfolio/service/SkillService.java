package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.ProficiencyLevel;
import com.hafizbahtiar.spring.features.portfolio.entity.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for skill management.
 * Handles CRUD operations and display order management for user skills.
 */
public interface SkillService {

    /**
     * Create a new skill for a user.
     *
     * @param userId  User ID
     * @param request Skill creation request
     * @return Created SkillResponse
     */
    SkillResponse createSkill(Long userId, SkillRequest request);

    /**
     * Update an existing skill.
     *
     * @param skillId Skill ID
     * @param userId  User ID (for ownership validation)
     * @param request Update request
     * @return Updated SkillResponse
     */
    SkillResponse updateSkill(Long skillId, Long userId, SkillRequest request);

    /**
     * Delete a skill.
     *
     * @param skillId Skill ID
     * @param userId  User ID (for ownership validation)
     */
    void deleteSkill(Long skillId, Long userId);

    /**
     * Bulk delete skills.
     *
     * @param userId User ID (for ownership validation)
     * @param ids    List of skill IDs to delete
     * @return BulkDeleteResponse with deleted count and failed IDs
     */
    BulkDeleteResponse bulkDeleteSkills(Long userId, List<Long> ids);

    /**
     * Get skill by ID.
     *
     * @param skillId Skill ID
     * @param userId  User ID (for ownership validation)
     * @return SkillResponse
     */
    SkillResponse getSkill(Long skillId, Long userId);

    /**
     * Get all skills for a user.
     *
     * @param userId User ID
     * @return List of SkillResponse
     */
    List<SkillResponse> getUserSkills(Long userId);

    /**
     * Get all skills for a user with pagination.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of SkillResponse
     */
    Page<SkillResponse> getUserSkills(Long userId, Pageable pageable);

    /**
     * Get skills for a user filtered by category.
     *
     * @param userId   User ID
     * @param category Skill category (optional)
     * @return List of SkillResponse
     */
    List<SkillResponse> getUserSkillsByCategory(Long userId, SkillCategory category);

    /**
     * Get skills for a user filtered by proficiency level.
     *
     * @param userId      User ID
     * @param proficiency Proficiency level (optional)
     * @return List of SkillResponse
     */
    List<SkillResponse> getUserSkillsByProficiency(Long userId, ProficiencyLevel proficiency);

    /**
     * Get active skills only for a user.
     *
     * @param userId User ID
     * @return List of SkillResponse
     */
    List<SkillResponse> getActiveUserSkills(Long userId);

    /**
     * Reorder skills by updating display order.
     *
     * @param userId        User ID
     * @param skillOrderMap Map of skill ID to display order
     */
    void reorderSkills(Long userId, Map<Long, Integer> skillOrderMap);
}
