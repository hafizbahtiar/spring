package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.ProjectRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Project entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "technologies", expression = "java(request.getTechnologies() != null ? request.getTechnologies() : null)")
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isFeatured", expression = "java(request.getIsFeatured() != null ? request.getIsFeatured() : false)")
    Project toEntity(ProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "technologies", expression = "java(request.getTechnologies() != null ? request.getTechnologies() : null)")
    void updateEntityFromRequest(ProjectRequest request, @MappingTarget Project project);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "technologies", expression = "java(project.getTechnologies() != null ? (java.util.List<String>) project.getTechnologies() : null)")
    @Mapping(target = "typeDisplayName", expression = "java(project.getType() != null ? project.getType().getDisplayName() : null)")
    @Mapping(target = "statusDisplayName", expression = "java(project.getStatus() != null ? project.getStatus().getDisplayName() : null)")
    @Mapping(target = "skills", ignore = true) // Will be set manually if needed
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);
}
