package com.hafizbahtiar.spring.features.portfolio.dto;

import com.hafizbahtiar.spring.features.portfolio.entity.ProficiencyLevel;
import com.hafizbahtiar.spring.features.portfolio.entity.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a skill.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100, message = "Skill name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Skill category is required")
    private SkillCategory category;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiency;

    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    private String icon;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Integer displayOrder;

    private Boolean isActive = true;
}
