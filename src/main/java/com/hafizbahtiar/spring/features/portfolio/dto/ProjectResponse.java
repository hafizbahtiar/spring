package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectStatus;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for project details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private List<String> technologies;
    private String githubUrl;
    private String liveUrl;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String address;
    private Double latitude;
    private Double longitude;
    private ProjectType type;
    private String typeDisplayName;
    private ProjectStatus status;
    private String statusDisplayName;
    private Boolean isFeatured;
    private Integer displayOrder;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Nested skill summaries (optional, if project has associated skills)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SkillResponse.Summary> skills;
}
