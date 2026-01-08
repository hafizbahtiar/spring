package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.SkillRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.ProficiencyLevel;
import com.hafizbahtiar.spring.features.portfolio.entity.Skill;
import com.hafizbahtiar.spring.features.portfolio.entity.SkillCategory;
import com.hafizbahtiar.spring.features.portfolio.mapper.SkillMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.SkillRepository;
import com.hafizbahtiar.spring.features.portfolio.exception.SkillNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SkillService.
 * Handles skill CRUD operations and display order management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
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
    public SkillResponse createSkill(Long userId, SkillRequest request) {
        log.debug("Creating skill for user ID: {}, skill name: {}", userId, request.getName());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Map request to entity
        Skill skill = skillMapper.toEntity(request);
        skill.setUser(user);

        // Set display order if not provided
        if (skill.getDisplayOrder() == null) {
            long maxOrder = skillRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Skill::getDisplayOrder)
                    .max()
                    .orElse(-1);
            skill.setDisplayOrder((int) (maxOrder + 1));
        }

        long startTime = System.currentTimeMillis();
        Skill savedSkill = skillRepository.save(skill);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Skill created successfully with ID: {} for user ID: {}", savedSkill.getId(), userId);

        // Log skill creation
        portfolioLoggingService.logSkillCreated(
                savedSkill.getId(),
                userId,
                savedSkill.getName(),
                getCurrentRequest(),
                responseTime);

        return skillMapper.toResponse(savedSkill);
    }

    @Override
    public SkillResponse updateSkill(Long skillId, Long userId, SkillRequest request) {
        log.debug("Updating skill ID: {} for user ID: {}", skillId, userId);

        // Validate skill exists and belongs to user
        Skill skill = skillRepository.findByUserIdAndId(userId, skillId)
                .orElseThrow(() -> SkillNotFoundException.byIdAndUser(skillId, userId));

        // Update entity from request
        skillMapper.updateEntityFromRequest(request, skill);

        long startTime = System.currentTimeMillis();
        Skill updatedSkill = skillRepository.save(skill);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Skill updated successfully with ID: {}", updatedSkill.getId());

        // Log skill update
        portfolioLoggingService.logSkillUpdated(
                updatedSkill.getId(),
                userId,
                updatedSkill.getName(),
                getCurrentRequest(),
                responseTime);

        return skillMapper.toResponse(updatedSkill);
    }

    @Override
    public void deleteSkill(Long skillId, Long userId) {
        log.debug("Deleting skill ID: {} for user ID: {}", skillId, userId);

        // Validate skill exists and belongs to user
        Skill skill = skillRepository.findByUserIdAndId(userId, skillId)
                .orElseThrow(() -> SkillNotFoundException.byIdAndUser(skillId, userId));

        String skillName = skill.getName();
        skillRepository.delete(skill);
        log.info("Skill deleted successfully with ID: {}", skillId);

        // Log skill deletion
        portfolioLoggingService.logSkillDeleted(skillId, userId, skillName, getCurrentRequest());
    }

    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteSkills(Long userId, List<Long> ids) {
        log.debug("Bulk deleting skills for user ID: {}, IDs: {}", userId, ids);

        List<Long> failedIds = new ArrayList<>();
        int deletedCount = 0;

        for (Long skillId : ids) {
            try {
                // Validate skill exists and belongs to user
                Skill skill = skillRepository.findByUserIdAndId(userId, skillId)
                        .orElseThrow(() -> SkillNotFoundException.byIdAndUser(skillId, userId));

                String skillName = skill.getName();
                skillRepository.delete(skill);
                deletedCount++;

                // Log individual skill deletion
                portfolioLoggingService.logSkillDeleted(skillId, userId, skillName, getCurrentRequest());
            } catch (Exception e) {
                log.warn("Failed to delete skill ID: {} for user ID: {} - {}", skillId, userId, e.getMessage());
                failedIds.add(skillId);
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
    public SkillResponse getSkill(Long skillId, Long userId) {
        log.debug("Fetching skill ID: {} for user ID: {}", skillId, userId);

        Skill skill = skillRepository.findByUserIdAndId(userId, skillId)
                .orElseThrow(() -> SkillNotFoundException.byIdAndUser(skillId, userId));

        return skillMapper.toResponse(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getUserSkills(Long userId) {
        log.debug("Fetching all skills for user ID: {}", userId);
        List<Skill> skills = skillRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return skillMapper.toResponseList(skills);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillResponse> getUserSkills(Long userId, Pageable pageable) {
        log.debug("Fetching skills (paginated) for user ID: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Skill> skills = skillRepository.findByUserIdOrderByDisplayOrderAsc(userId, pageable);
        return skills.map(skillMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getUserSkillsByCategory(Long userId, SkillCategory category) {
        log.debug("Fetching skills for user ID: {} by category: {}", userId, category);
        List<Skill> skills = category != null
                ? skillRepository.findByUserIdAndCategoryOrderByDisplayOrderAsc(userId, category)
                : skillRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return skillMapper.toResponseList(skills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getUserSkillsByProficiency(Long userId, ProficiencyLevel proficiency) {
        log.debug("Fetching skills for user ID: {} by proficiency: {}", userId, proficiency);
        List<Skill> skills = proficiency != null
                ? skillRepository.findByUserIdAndProficiencyOrderByDisplayOrderAsc(userId, proficiency)
                : skillRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return skillMapper.toResponseList(skills);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getActiveUserSkills(Long userId) {
        log.debug("Fetching active skills for user ID: {}", userId);
        List<Skill> skills = skillRepository.findByUserIdAndIsActiveTrueOrderByDisplayOrderAsc(userId);
        return skillMapper.toResponseList(skills);
    }

    @Override
    public void reorderSkills(Long userId, Map<Long, Integer> skillOrderMap) {
        log.debug("Reordering skills for user ID: {}", userId);

        skillOrderMap.forEach((skillId, displayOrder) -> {
            Skill skill = skillRepository.findByUserIdAndId(userId, skillId)
                    .orElseThrow(() -> SkillNotFoundException.byIdAndUser(skillId, userId));
            skill.setDisplayOrder(displayOrder);
            skillRepository.save(skill);
        });

        log.info("Skills reordered successfully for user ID: {}", userId);

        // Log skills reorder
        portfolioLoggingService.logSkillsReordered(userId, getCurrentRequest());
    }
}
