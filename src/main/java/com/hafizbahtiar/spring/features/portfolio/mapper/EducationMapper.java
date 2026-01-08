package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.EducationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.EducationResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Education;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Education entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EducationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isCurrent", expression = "java(request.getIsCurrent() != null ? request.getIsCurrent() : false)")
    Education toEntity(EducationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(EducationRequest request, @MappingTarget Education education);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "degreeDisplayName", expression = "java(education.getDegree() != null ? education.getDegree().getDisplayName() : null)")
    @Mapping(target = "durationInMonths", expression = "java(education.getDurationInMonths())")
    @Mapping(target = "isHigherEducation", expression = "java(education.isHigherEducation())")
    EducationResponse toResponse(Education education);

    List<EducationResponse> toResponseList(List<Education> educations);
}
