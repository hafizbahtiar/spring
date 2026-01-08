package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hafizbahtiar.spring.features.portfolio.entity.ProficiencyLevel;
import com.hafizbahtiar.spring.features.portfolio.entity.SkillCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for skill details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private Long id;
    private Long userId;
    private String name;
    private SkillCategory category;
    private String categoryDisplayName;
    private ProficiencyLevel proficiency;
    private String proficiencyDisplayName;
    private String icon;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Inner class for summary responses (used in nested contexts)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String name;
        private SkillCategory category;
        private String categoryDisplayName;
        private ProficiencyLevel proficiency;
        private String proficiencyDisplayName;
        private String icon;
    }
}
