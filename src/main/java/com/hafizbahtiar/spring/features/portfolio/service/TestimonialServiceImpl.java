package com.hafizbahtiar.spring.features.portfolio.service;

import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Project;
import com.hafizbahtiar.spring.features.portfolio.entity.Testimonial;
import com.hafizbahtiar.spring.features.portfolio.exception.PortfolioException;
import com.hafizbahtiar.spring.features.portfolio.exception.ProjectNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.exception.TestimonialNotFoundException;
import com.hafizbahtiar.spring.features.portfolio.mapper.TestimonialMapper;
import com.hafizbahtiar.spring.features.portfolio.repository.ProjectRepository;
import com.hafizbahtiar.spring.features.portfolio.repository.TestimonialRepository;
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
 * Implementation of TestimonialService.
 * Handles testimonial CRUD operations, approval workflow, featured status, and
 * display order management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TestimonialServiceImpl implements TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final TestimonialMapper testimonialMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
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

    /**
     * Validate rating is between 1 and 5
     */
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new PortfolioException("Rating must be between 1 and 5");
        }
    }

    @Override
    public TestimonialResponse createTestimonial(Long userId, TestimonialRequest request) {
        log.debug("Creating testimonial for user ID: {}, author: {}", userId, request.getAuthorName());

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Validate rating
        validateRating(request.getRating());

        // Map request to entity
        Testimonial testimonial = testimonialMapper.toEntity(request);
        testimonial.setUser(user);

        // Set project if provided
        if (request.getProjectId() != null) {
            Project project = projectRepository.findByUserIdAndId(userId, request.getProjectId())
                    .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(request.getProjectId(), userId));
            testimonial.setProject(project);
        }

        // Set display order if not provided
        if (testimonial.getDisplayOrder() == null) {
            long maxOrder = testimonialRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
                    .mapToLong(Testimonial::getDisplayOrder)
                    .max()
                    .orElse(-1);
            testimonial.setDisplayOrder((int) (maxOrder + 1));
        }

        long startTime = System.currentTimeMillis();
        Testimonial savedTestimonial = testimonialRepository.save(testimonial);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Testimonial created successfully with ID: {} for user ID: {}", savedTestimonial.getId(), userId);

        // Log testimonial creation
        portfolioLoggingService.logTestimonialCreated(
                savedTestimonial.getId(),
                userId,
                savedTestimonial.getAuthorName(),
                getCurrentRequest(),
                responseTime);

        return testimonialMapper.toResponse(savedTestimonial);
    }

    @Override
    public TestimonialResponse updateTestimonial(Long testimonialId, Long userId, TestimonialRequest request) {
        log.debug("Updating testimonial ID: {} for user ID: {}", testimonialId, userId);

        // Validate testimonial exists and belongs to user
        Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));

        // Validate rating if provided
        if (request.getRating() != null) {
            validateRating(request.getRating());
        }

        // Update entity from request
        testimonialMapper.updateEntityFromRequest(request, testimonial);

        // Update project if provided
        if (request.getProjectId() != null) {
            Project project = projectRepository.findByUserIdAndId(userId, request.getProjectId())
                    .orElseThrow(() -> ProjectNotFoundException.byIdAndUser(request.getProjectId(), userId));
            testimonial.setProject(project);
        } else if (request.getProjectId() == null && testimonial.getProject() != null) {
            // Remove project reference if explicitly set to null
            testimonial.setProject(null);
        }

        long startTime = System.currentTimeMillis();
        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Testimonial updated successfully with ID: {}", updatedTestimonial.getId());

        // Log testimonial update
        portfolioLoggingService.logTestimonialUpdated(
                updatedTestimonial.getId(),
                userId,
                updatedTestimonial.getAuthorName(),
                getCurrentRequest(),
                responseTime);

        return testimonialMapper.toResponse(updatedTestimonial);
    }

    @Override
    public void deleteTestimonial(Long testimonialId, Long userId) {
        log.debug("Deleting testimonial ID: {} for user ID: {}", testimonialId, userId);

        // Validate testimonial exists and belongs to user
        Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));

        String authorName = testimonial.getAuthorName();
        testimonialRepository.delete(testimonial);
        log.info("Testimonial deleted successfully with ID: {}", testimonialId);

        // Log testimonial deletion
        portfolioLoggingService.logTestimonialDeleted(testimonialId, userId, authorName, getCurrentRequest());
    }

    @Override
    @Transactional
    public BulkDeleteResponse bulkDeleteTestimonials(Long userId, List<Long> ids) {
        log.debug("Bulk deleting testimonials for user ID: {}, IDs: {}", userId, ids);

        List<Long> failedIds = new ArrayList<>();
        int deletedCount = 0;

        for (Long testimonialId : ids) {
            try {
                // Validate testimonial exists and belongs to user
                Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                        .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));

                String authorName = testimonial.getAuthorName();
                testimonialRepository.delete(testimonial);
                deletedCount++;

                // Log individual testimonial deletion
                portfolioLoggingService.logTestimonialDeleted(testimonialId, userId, authorName, getCurrentRequest());
            } catch (Exception e) {
                log.warn("Failed to delete testimonial ID: {} for user ID: {} - {}", testimonialId, userId,
                        e.getMessage());
                failedIds.add(testimonialId);
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
    public TestimonialResponse getTestimonial(Long testimonialId, Long userId) {
        log.debug("Fetching testimonial ID: {} for user ID: {}", testimonialId, userId);

        Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));

        return testimonialMapper.toResponse(testimonial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getUserTestimonials(Long userId) {
        log.debug("Fetching all testimonials for user ID: {}", userId);
        List<Testimonial> testimonials = testimonialRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return testimonialMapper.toResponseList(testimonials);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestimonialResponse> getUserTestimonials(Long userId, Pageable pageable) {
        log.debug("Fetching testimonials (paginated) for user ID: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Testimonial> testimonials = testimonialRepository.findByUserIdOrderByDisplayOrderAsc(userId, pageable);
        return testimonials.map(testimonialMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getApprovedTestimonials(Long userId) {
        log.debug("Fetching approved testimonials for user ID: {}", userId);
        List<Testimonial> testimonials = testimonialRepository
                .findByUserIdAndIsApprovedTrueOrderByDisplayOrderAsc(userId);
        return testimonialMapper.toResponseList(testimonials);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getFeaturedTestimonials(Long userId) {
        log.debug("Fetching featured testimonials for user ID: {}", userId);
        List<Testimonial> testimonials = testimonialRepository
                .findByUserIdAndIsFeaturedTrueOrderByDisplayOrderAsc(userId);
        return testimonialMapper.toResponseList(testimonials);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getUserTestimonialsByRating(Long userId, Integer rating) {
        log.debug("Fetching testimonials for user ID: {} by rating: {}", userId, rating);
        validateRating(rating);
        List<Testimonial> testimonials = testimonialRepository.findByUserIdAndRatingOrderByDisplayOrderAsc(userId,
                rating);
        return testimonialMapper.toResponseList(testimonials);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getUserTestimonialsByProject(Long userId, Long projectId) {
        log.debug("Fetching testimonials for user ID: {} by project ID: {}", userId, projectId);
        List<Testimonial> testimonials = projectId != null
                ? testimonialRepository.findByUserIdAndProjectIdOrderByDisplayOrderAsc(userId, projectId)
                : testimonialRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return testimonialMapper.toResponseList(testimonials);
    }

    @Override
    public TestimonialResponse setFeatured(Long testimonialId, Long userId, Boolean featured) {
        log.debug("Setting testimonial ID: {} featured status to: {} for user ID: {}", testimonialId, featured, userId);

        // Validate testimonial exists and belongs to user
        Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));

        testimonial.setFeatured(featured != null && featured);

        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        log.info("Testimonial featured status updated successfully with ID: {}", updatedTestimonial.getId());

        // Log featured status change
        portfolioLoggingService.logTestimonialFeatured(
                updatedTestimonial.getId(),
                userId,
                updatedTestimonial.getAuthorName(),
                updatedTestimonial.isFeatured(),
                getCurrentRequest());

        return testimonialMapper.toResponse(updatedTestimonial);
    }

    @Override
    public TestimonialResponse approveTestimonial(Long testimonialId, Long userId) {
        log.debug("Approving testimonial ID: {} for user ID: {}", testimonialId, userId);

        // Validate testimonial exists and belongs to user
        Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));

        boolean previousApproved = testimonial.isApproved();
        testimonial.approve();

        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        log.info("Testimonial approved successfully with ID: {}", updatedTestimonial.getId());

        // Log approval
        portfolioLoggingService.logTestimonialApproved(
                updatedTestimonial.getId(),
                userId,
                updatedTestimonial.getAuthorName(),
                previousApproved,
                getCurrentRequest());

        return testimonialMapper.toResponse(updatedTestimonial);
    }

    @Override
    public void reorderTestimonials(Long userId, Map<Long, Integer> testimonialOrderMap) {
        log.debug("Reordering testimonials for user ID: {}", userId);

        testimonialOrderMap.forEach((testimonialId, displayOrder) -> {
            Testimonial testimonial = testimonialRepository.findByUserIdAndId(userId, testimonialId)
                    .orElseThrow(() -> TestimonialNotFoundException.byIdAndUser(testimonialId, userId));
            testimonial.setDisplayOrder(displayOrder);
            testimonialRepository.save(testimonial);
        });

        log.info("Testimonials reordered successfully for user ID: {}", userId);

        // Log testimonials reorder
        portfolioLoggingService.logTestimonialsReordered(userId, getCurrentRequest());
    }
}
