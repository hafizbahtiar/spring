package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.common.dto.BulkDeleteResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.DegreeType;
import com.hafizbahtiar.spring.features.portfolio.entity.Education;
import com.hafizbahtiar.spring.features.portfolio.mapper.EducationMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.EducationRepository;
import com.hafizbahtiar.spring.features.portfolio.exception.EducationNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
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
 * Implementation of EducationService.
 * Handles education CRUD operations, date validation, and display order
 * management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final EducationMapper educationMapper;
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
    public EducationResponse createEducation(Long userId, EducationRequest request) {
        log.debug("Creating education for user ID: {}, institution: {}", userId, request.getInstitution());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate date range
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw PortfolioException.invalidDateRange("End date must be after start date");
        }

        // Handle current education logic
        if (Boolean.TRUE.equals(request.getIsCurrent())) {
            request.setEndDate(null);
        }

        // Map request to entity
        Education education = educationMapper.toEntity(request);
        education.setUser(user);

        // Set display order if not provided
        if (education.getDisplayOrder() == null) {
            long maxOrder = educationRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Education::getDisplayOrder)
                    .max()
                    .orElse(-1);
            education.setDisplayOrder((int) (maxOrder + 1));
        }

        // Set as current if endDate is null
        if (education.getEndDate() == null) {
            education.setAsCurrent();
        }

        long startTime = System.currentTimeMillis();
        Education savedEducation = educationRepository.save(education);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Education created successfully with ID: {} for user ID: {}", savedEducation.getId(), userId);

        // Log education creation
        portfolioLoggingService.logEducationCreated(
                savedEducation.getId(),
                userId,
                savedEducation.getInstitution(),
                savedEducation.getFieldOfStudy(),
                getCurrentRequest(),
                responseTime);

        return educationMapper.toResponse(savedEducation);
    }

    @Override
    public EducationResponse updateEducation(Long educationId, Long userId, EducationRequest request) {
        log.debug("Updating education ID: {} for user ID: {}", educationId, userId);

        // Validate education exists and belongs to user
        Education education = educationRepository.findByUserIdAndId(userId, educationId)
                .orElseThrow(() -> EducationNotFoundException.byIdAndUser(educationId, userId));

        // Validate date range
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw PortfolioException.invalidDateRange("End date must be after start date");
        }

        // Handle current education logic
        if (Boolean.TRUE.equals(request.getIsCurrent())) {
            request.setEndDate(null);
            education.setAsCurrent();
        } else if (request.getEndDate() != null) {
            education.setAsCompleted(request.getEndDate());
        }

        // Update entity from request
        educationMapper.updateEntityFromRequest(request, education);

        long startTime = System.currentTimeMillis();
        Education updatedEducation = educationRepository.save(education);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Education updated successfully with ID: {}", updatedEducation.getId());

        // Log education update
        portfolioLoggingService.logEducationUpdated(
                updatedEducation.getId(),
                userId,
                updatedEducation.getInstitution(),
                updatedEducation.getFieldOfStudy(),
                getCurrentRequest(),
                responseTime);

        return educationMapper.toResponse(updatedEducation);
    }

    @Override
    public void deleteEducation(Long educationId, Long userId) {
        log.debug("Deleting education ID: {} for user ID: {}", educationId, userId);

        // Validate education exists and belongs to user
        Education education = educationRepository.findByUserIdAndId(userId, educationId)
                .orElseThrow(() -> EducationNotFoundException.byIdAndUser(educationId, userId));

        String institution = education.getInstitution();
        String fieldOfStudy = education.getFieldOfStudy();
        educationRepository.delete(education);
        log.info("Education deleted successfully with ID: {}", educationId);

        // Log education deletion
        portfolioLoggingService.logEducationDeleted(educationId, userId, institution, fieldOfStudy,
                getCurrentRequest());
    }

    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteEducations(Long userId, List<Long> ids) {
        log.debug("Bulk deleting educations for user ID: {}, IDs: {}", userId, ids);

        List<Long> failedIds = new ArrayList<>();
        int deletedCount = 0;

        for (Long educationId : ids) {
            try {
                // Validate education exists and belongs to user
                Education education = educationRepository.findByUserIdAndId(userId, educationId)
                        .orElseThrow(() -> EducationNotFoundException.byIdAndUser(educationId, userId));

                String institution = education.getInstitution();
                String fieldOfStudy = education.getFieldOfStudy();
                educationRepository.delete(education);
                deletedCount++;

                // Log individual education deletion
                portfolioLoggingService.logEducationDeleted(educationId, userId, institution, fieldOfStudy,
                        getCurrentRequest());
            } catch (Exception e) {
                log.warn("Failed to delete education ID: {} for user ID: {} - {}", educationId, userId, e.getMessage());
                failedIds.add(educationId);
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
    public EducationResponse getEducation(Long educationId, Long userId) {
        log.debug("Fetching education ID: {} for user ID: {}", educationId, userId);

        Education education = educationRepository.findByUserIdAndId(userId, educationId)
                .orElseThrow(() -> EducationNotFoundException.byIdAndUser(educationId, userId));

        return educationMapper.toResponse(education);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getUserEducations(Long userId) {
        log.debug("Fetching all educations for user ID: {}", userId);
        List<Education> educations = educationRepository.findByUserIdOrderByStartDateDesc(userId);
        return educationMapper.toResponseList(educations);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EducationResponse> getUserEducations(Long userId, Pageable pageable) {
        log.debug("Fetching educations (paginated) for user ID: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Education> educations = educationRepository.findByUserIdOrderByDisplayOrderAsc(userId, pageable);
        return educations.map(educationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getCurrentEducations(Long userId) {
        log.debug("Fetching current educations for user ID: {}", userId);
        List<Education> educations = educationRepository.findByUserIdAndIsCurrentTrueOrderByStartDateDesc(userId);
        return educationMapper.toResponseList(educations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getCompletedEducations(Long userId) {
        log.debug("Fetching completed educations for user ID: {}", userId);
        List<Education> educations = educationRepository.findByUserIdAndIsCurrentFalseOrderByStartDateDesc(userId);
        return educationMapper.toResponseList(educations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getUserEducationsByDegree(Long userId, DegreeType degree) {
        log.debug("Fetching educations for user ID: {} by degree: {}", userId, degree);
        List<Education> educations = degree != null
                ? educationRepository.findByUserIdAndDegreeOrderByStartDateDesc(userId, degree)
                : educationRepository.findByUserIdOrderByStartDateDesc(userId);
        return educationMapper.toResponseList(educations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getUserEducationsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching educations for user ID: {} in date range: {} to {}", userId, startDate, endDate);
        List<Education> educations = educationRepository.findByUserIdAndStartDateBetween(userId, startDate, endDate);
        return educationMapper.toResponseList(educations);
    }

    @Override
    public void reorderEducations(Long userId, Map<Long, Integer> educationOrderMap) {
        log.debug("Reordering educations for user ID: {}", userId);

        educationOrderMap.forEach((educationId, displayOrder) -> {
            Education education = educationRepository.findByUserIdAndId(userId, educationId)
                    .orElseThrow(() -> EducationNotFoundException.byIdAndUser(educationId, userId));
            education.setDisplayOrder(displayOrder);
            educationRepository.save(education);
        });

        log.info("Educations reordered successfully for user ID: {}", userId);

        // Log educations reorder
        portfolioLoggingService.logEducationsReordered(userId, getCurrentRequest());
    }
}
