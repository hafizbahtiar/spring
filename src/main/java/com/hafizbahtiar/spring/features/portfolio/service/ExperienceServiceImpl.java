package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.EmploymentType;
import com.hafizbahtiar.spring.features.portfolio.entity.Experience;
import com.hafizbahtiar.spring.features.portfolio.mapper.ExperienceMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.ExperienceRepository;
import com.hafizbahtiar.spring.features.portfolio.exception.ExperienceNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ExperienceService.
 * Handles experience CRUD operations, date validation, and display order
 * management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceMapper experienceMapper;
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
    public ExperienceResponse createExperience(Long userId, ExperienceRequest request) {
        log.debug("Creating experience for user ID: {}, company: {}", userId, request.getCompany());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate date range
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw PortfolioException.invalidDateRange("End date must be after start date");
        }

        // Handle current experience logic
        if (Boolean.TRUE.equals(request.getIsCurrent())) {
            request.setEndDate(null);
        }

        // Map request to entity
        Experience experience = experienceMapper.toEntity(request);
        experience.setUser(user);

        // Set display order if not provided
        if (experience.getDisplayOrder() == null) {
            long maxOrder = experienceRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Experience::getDisplayOrder)
                    .max()
                    .orElse(-1);
            experience.setDisplayOrder((int) (maxOrder + 1));
        }

        // Set as current if endDate is null
        if (experience.getEndDate() == null) {
            experience.setAsCurrent();
        }

        long startTime = System.currentTimeMillis();
        Experience savedExperience = experienceRepository.save(experience);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Experience created successfully with ID: {} for user ID: {}", savedExperience.getId(), userId);

        // Log experience creation
        portfolioLoggingService.logExperienceCreated(
                savedExperience.getId(),
                userId,
                savedExperience.getCompany(),
                savedExperience.getPosition(),
                getCurrentRequest(),
                responseTime);

        return experienceMapper.toResponse(savedExperience);
    }

    @Override
    public ExperienceResponse updateExperience(Long experienceId, Long userId, ExperienceRequest request) {
        log.debug("Updating experience ID: {} for user ID: {}", experienceId, userId);

        // Validate experience exists and belongs to user
        Experience experience = experienceRepository.findByUserIdAndId(userId, experienceId)
                .orElseThrow(() -> ExperienceNotFoundException.byIdAndUser(experienceId, userId));

        // Validate date range
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw PortfolioException.invalidDateRange("End date must be after start date");
        }

        // Handle current experience logic
        if (Boolean.TRUE.equals(request.getIsCurrent())) {
            request.setEndDate(null);
            experience.setAsCurrent();
        } else if (request.getEndDate() != null) {
            experience.setAsPast(request.getEndDate());
        }

        // Update entity from request
        experienceMapper.updateEntityFromRequest(request, experience);

        long startTime = System.currentTimeMillis();
        Experience updatedExperience = experienceRepository.save(experience);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Experience updated successfully with ID: {}", updatedExperience.getId());

        // Log experience update
        portfolioLoggingService.logExperienceUpdated(
                updatedExperience.getId(),
                userId,
                updatedExperience.getCompany(),
                updatedExperience.getPosition(),
                getCurrentRequest(),
                responseTime);

        return experienceMapper.toResponse(updatedExperience);
    }

    @Override
    public void deleteExperience(Long experienceId, Long userId) {
        log.debug("Deleting experience ID: {} for user ID: {}", experienceId, userId);

        // Validate experience exists and belongs to user
        Experience experience = experienceRepository.findByUserIdAndId(userId, experienceId)
                .orElseThrow(() -> ExperienceNotFoundException.byIdAndUser(experienceId, userId));

        String company = experience.getCompany();
        String position = experience.getPosition();
        experienceRepository.delete(experience);
        log.info("Experience deleted successfully with ID: {}", experienceId);

        // Log experience deletion
        portfolioLoggingService.logExperienceDeleted(experienceId, userId, company, position, getCurrentRequest());
    }

    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteExperiences(Long userId, List<Long> ids) {
        log.debug("Bulk deleting experiences for user ID: {}, IDs: {}", userId, ids);

        List<Long> failedIds = new ArrayList<>();
        int deletedCount = 0;

        for (Long experienceId : ids) {
            try {
                // Validate experience exists and belongs to user
                Experience experience = experienceRepository.findByUserIdAndId(userId, experienceId)
                        .orElseThrow(() -> ExperienceNotFoundException.byIdAndUser(experienceId, userId));

                String company = experience.getCompany();
                String position = experience.getPosition();
                experienceRepository.delete(experience);
                deletedCount++;

                // Log individual experience deletion
                portfolioLoggingService.logExperienceDeleted(experienceId, userId, company, position,
                        getCurrentRequest());
            } catch (Exception e) {
                log.warn("Failed to delete experience ID: {} for user ID: {} - {}", experienceId, userId,
                        e.getMessage());
                failedIds.add(experienceId);
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
    public ExperienceResponse getExperience(Long experienceId, Long userId) {
        log.debug("Fetching experience ID: {} for user ID: {}", experienceId, userId);

        Experience experience = experienceRepository.findByUserIdAndId(userId, experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found or does not belong to user"));

        return experienceMapper.toResponse(experience);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getUserExperiences(Long userId) {
        log.debug("Fetching all experiences for user ID: {}", userId);
        List<Experience> experiences = experienceRepository.findByUserIdOrderByStartDateDesc(userId);
        return experienceMapper.toResponseList(experiences);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExperienceResponse> getUserExperiences(Long userId, Pageable pageable) {
        log.debug("Fetching experiences (paginated) for user ID: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Experience> experiences = experienceRepository.findByUserIdOrderByDisplayOrderAsc(userId, pageable);
        return experiences.map(experienceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getCurrentExperiences(Long userId) {
        log.debug("Fetching current experiences for user ID: {}", userId);
        List<Experience> experiences = experienceRepository.findByUserIdAndIsCurrentTrueOrderByStartDateDesc(userId);
        return experienceMapper.toResponseList(experiences);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getPastExperiences(Long userId) {
        log.debug("Fetching past experiences for user ID: {}", userId);
        List<Experience> experiences = experienceRepository.findByUserIdAndIsCurrentFalseOrderByStartDateDesc(userId);
        return experienceMapper.toResponseList(experiences);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getUserExperiencesByType(Long userId, EmploymentType employmentType) {
        log.debug("Fetching experiences for user ID: {} by type: {}", userId, employmentType);
        List<Experience> experiences = employmentType != null
                ? experienceRepository.findByUserIdAndEmploymentTypeOrderByStartDateDesc(userId, employmentType)
                : experienceRepository.findByUserIdOrderByStartDateDesc(userId);
        return experienceMapper.toResponseList(experiences);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getUserExperiencesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching experiences for user ID: {} in date range: {} to {}", userId, startDate, endDate);
        List<Experience> experiences = experienceRepository.findByUserIdAndStartDateBetween(userId, startDate, endDate);
        return experienceMapper.toResponseList(experiences);
    }

    @Override
    public void reorderExperiences(Long userId, Map<Long, Integer> experienceOrderMap) {
        log.debug("Reordering experiences for user ID: {}", userId);

        experienceOrderMap.forEach((experienceId, displayOrder) -> {
            Experience experience = experienceRepository.findByUserIdAndId(userId, experienceId)
                    .orElseThrow(() -> ExperienceNotFoundException.byIdAndUser(experienceId, userId));
            experience.setDisplayOrder(displayOrder);
            experienceRepository.save(experience);
        });

        log.info("Experiences reordered successfully for user ID: {}", userId);

        // Log experiences reorder
        portfolioLoggingService.logExperiencesReordered(userId, getCurrentRequest());
    }
}
