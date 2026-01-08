package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for complete portfolio summary.
 * Contains all portfolio items grouped by type.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {

    private Long userId;
    private String username; // Optional, for public portfolio view

    /**
     * User's skills grouped by category
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SkillResponse> skills;

    /**
     * User's work experiences
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ExperienceResponse> experiences;

    /**
     * User's projects grouped by type
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ProjectResponse> projects;

    /**
     * User's education history
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EducationResponse> educations;

    /**
     * Summary statistics
     */
    private PortfolioStats stats;

    /**
     * Inner class for portfolio statistics
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioStats {
        private Long totalSkills;
        private Long totalExperiences;
        private Long totalProjects;
        private Long totalEducations;
        private Long featuredProjects;
        private Long currentExperiences;
        private Long currentEducations;
    }
}

