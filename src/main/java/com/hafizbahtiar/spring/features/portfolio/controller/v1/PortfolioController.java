package com.hafizbahtiar.spring.features.portfolio.controller.v1;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.security.UserPrincipal;
import com.hafizbahtiar.spring.common.util.ResponseUtils;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioSummaryResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectResponse;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillResponse;
import com.hafizbahtiar.spring.features.portfolio.service.EducationService;
import com.hafizbahtiar.spring.features.portfolio.service.ExperienceService;
import com.hafizbahtiar.spring.features.portfolio.service.ProjectService;
import com.hafizbahtiar.spring.features.portfolio.service.SkillService;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for portfolio summary endpoint.
 * Returns complete portfolio summary with all items grouped by type.
 */
@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final SkillService skillService;
    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final EducationService educationService;
    private final UserRepository userRepository;

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
     * Get complete portfolio summary
     * GET /api/v1/portfolio/summary
     * Requires: Authenticated user
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolioSummary() {
        Long userId = getCurrentUserId();
        log.debug("Fetching portfolio summary for user ID: {}", userId);

        // Fetch all portfolio items
        List<SkillResponse> skills = skillService.getActiveUserSkills(userId);
        List<ExperienceResponse> experiences = experienceService.getUserExperiences(userId);
        List<ProjectResponse> projects = projectService.getUserProjects(userId);
        List<EducationResponse> educations = educationService.getUserEducations(userId);

        // Get user info
        String username = userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse(null);

        // Build statistics
        PortfolioSummaryResponse.PortfolioStats stats = PortfolioSummaryResponse.PortfolioStats.builder()
                .totalSkills((long) skills.size())
                .totalExperiences((long) experiences.size())
                .totalProjects((long) projects.size())
                .totalEducations((long) educations.size())
                .featuredProjects(projects.stream().filter(ProjectResponse::getIsFeatured).count())
                .currentExperiences(experiences.stream().filter(ExperienceResponse::getIsCurrent).count())
                .currentEducations(educations.stream().filter(EducationResponse::getIsCurrent).count())
                .build();

        // Build summary response
        PortfolioSummaryResponse summary = PortfolioSummaryResponse.builder()
                .userId(userId)
                .username(username)
                .skills(skills)
                .experiences(experiences)
                .projects(projects)
                .educations(educations)
                .stats(stats)
                .build();

        return ResponseUtils.ok(summary);
    }

    /**
     * Get public portfolio view by username
     * GET /api/v1/portfolio/public/username/{username}
     * Requires: None (public endpoint)
     */
    @GetMapping("/public/username/{username}")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPublicPortfolioByUsername(
            @PathVariable String username) {
        log.debug("Fetching public portfolio for username: {}", username);

        // Find user by username
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> UserNotFoundException.byUsername(username));

        // Check if user is active
        if (!user.getActive()) {
            throw new RuntimeException("User portfolio is not available");
        }

        Long userId = user.getId();

        // Fetch all portfolio items (only active/public items)
        List<SkillResponse> skills = skillService.getActiveUserSkills(userId);
        List<ExperienceResponse> experiences = experienceService.getUserExperiences(userId);
        List<ProjectResponse> projects = projectService.getUserProjects(userId);
        List<EducationResponse> educations = educationService.getUserEducations(userId);

        // Build statistics
        PortfolioSummaryResponse.PortfolioStats stats = PortfolioSummaryResponse.PortfolioStats.builder()
                .totalSkills((long) skills.size())
                .totalExperiences((long) experiences.size())
                .totalProjects((long) projects.size())
                .totalEducations((long) educations.size())
                .featuredProjects(projects.stream().filter(ProjectResponse::getIsFeatured).count())
                .currentExperiences(experiences.stream().filter(ExperienceResponse::getIsCurrent).count())
                .currentEducations(educations.stream().filter(EducationResponse::getIsCurrent).count())
                .build();

        // Build summary response
        PortfolioSummaryResponse summary = PortfolioSummaryResponse.builder()
                .userId(userId)
                .username(username)
                .skills(skills)
                .experiences(experiences)
                .projects(projects)
                .educations(educations)
                .stats(stats)
                .build();

        return ResponseUtils.ok(summary);
    }

    /**
     * Get public portfolio view by user ID
     * GET /api/v1/portfolio/public/user/{userId}
     * Requires: None (public endpoint)
     */
    @GetMapping("/public/user/{userId}")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPublicPortfolioByUserId(
            @PathVariable Long userId) {
        log.debug("Fetching public portfolio for user ID: {}", userId);

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Check if user is active
        if (!user.getActive()) {
            throw new RuntimeException("User portfolio is not available");
        }

        // Fetch all portfolio items (only active/public items)
        List<SkillResponse> skills = skillService.getActiveUserSkills(userId);
        List<ExperienceResponse> experiences = experienceService.getUserExperiences(userId);
        List<ProjectResponse> projects = projectService.getUserProjects(userId);
        List<EducationResponse> educations = educationService.getUserEducations(userId);

        // Build statistics
        PortfolioSummaryResponse.PortfolioStats stats = PortfolioSummaryResponse.PortfolioStats.builder()
                .totalSkills((long) skills.size())
                .totalExperiences((long) experiences.size())
                .totalProjects((long) projects.size())
                .totalEducations((long) educations.size())
                .featuredProjects(projects.stream().filter(ProjectResponse::getIsFeatured).count())
                .currentExperiences(experiences.stream().filter(ExperienceResponse::getIsCurrent).count())
                .currentEducations(educations.stream().filter(EducationResponse::getIsCurrent).count())
                .build();

        // Build summary response
        PortfolioSummaryResponse summary = PortfolioSummaryResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .skills(skills)
                .experiences(experiences)
                .projects(projects)
                .educations(educations)
                .stats(stats)
                .build();

        return ResponseUtils.ok(summary);
    }
}
