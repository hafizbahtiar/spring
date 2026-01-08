package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ExperienceResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Experience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Experience entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExperienceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isCurrent", expression = "java(request.getIsCurrent() != null ? request.getIsCurrent() : false)")
    Experience toEntity(ExperienceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(ExperienceRequest request, @MappingTarget Experience experience);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "employmentTypeDisplayName", expression = "java(experience.getEmploymentType() != null ? experience.getEmploymentType().getDisplayName() : null)")
    @Mapping(target = "durationInMonths", expression = "java(experience.getDurationInMonths())")
    ExperienceResponse toResponse(Experience experience);

    List<ExperienceResponse> toResponseList(List<Experience> experiences);
}
