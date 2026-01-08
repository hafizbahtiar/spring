package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.SkillRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.SkillResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Skill entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SkillMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isActive", expression = "java(request.getIsActive() != null ? request.getIsActive() : true)")
    Skill toEntity(SkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(SkillRequest request, @MappingTarget Skill skill);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "categoryDisplayName", expression = "java(skill.getCategory() != null ? skill.getCategory().getDisplayName() : null)")
    @Mapping(target = "proficiencyDisplayName", expression = "java(skill.getProficiency() != null ? skill.getProficiency().getDisplayName() : null)")
    SkillResponse toResponse(Skill skill);

    List<SkillResponse> toResponseList(List<Skill> skills);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "categoryDisplayName", expression = "java(skill.getCategory() != null ? skill.getCategory().getDisplayName() : null)")
    @Mapping(target = "proficiency", source = "proficiency")
    @Mapping(target = "proficiencyDisplayName", expression = "java(skill.getProficiency() != null ? skill.getProficiency().getDisplayName() : null)")
    @Mapping(target = "icon", source = "icon")
    SkillResponse.Summary toSummary(Skill skill);

    List<SkillResponse.Summary> toSummaryList(List<Skill> skills);
}
