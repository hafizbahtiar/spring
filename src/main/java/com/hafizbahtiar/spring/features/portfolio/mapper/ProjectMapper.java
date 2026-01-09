package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.ProjectRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.ProjectResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Mapping(target = "images", expression = "java(request.getImages() != null ? request.getImages() : null)")
    @Mapping(target = "roadmap", expression = "java(request.getRoadmap() != null ? convertRoadmapToMapList(request.getRoadmap()) : null)")
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isFeatured", expression = "java(request.getIsFeatured() != null ? request.getIsFeatured() : false)")
    Project toEntity(ProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "technologies", expression = "java(request.getTechnologies() != null ? request.getTechnologies() : null)")
    @Mapping(target = "images", expression = "java(request.getImages() != null ? request.getImages() : null)")
    @Mapping(target = "roadmap", expression = "java(request.getRoadmap() != null ? convertRoadmapToMapList(request.getRoadmap()) : null)")
    void updateEntityFromRequest(ProjectRequest request, @MappingTarget Project project);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "technologies", expression = "java(project.getTechnologies() != null ? (java.util.List<String>) project.getTechnologies() : null)")
    @Mapping(target = "images", expression = "java(project.getImages() != null ? (java.util.List<String>) project.getImages() : null)")
    @Mapping(target = "roadmap", expression = "java(project.getRoadmap() != null ? convertRoadmapToDto((java.util.List<java.util.Map<java.lang.String, java.lang.Object>>) project.getRoadmap()) : null)")
    @Mapping(target = "typeDisplayName", expression = "java(project.getType() != null ? project.getType().getDisplayName() : null)")
    @Mapping(target = "statusDisplayName", expression = "java(project.getStatus() != null ? project.getStatus().getDisplayName() : null)")
    @Mapping(target = "platformDisplayName", expression = "java(project.getPlatform() != null ? project.getPlatform().getDisplayName() : null)")
    @Mapping(target = "skills", ignore = true) // Will be set manually if needed
    ProjectResponse toResponse(Project project);

    /**
     * Convert List<ProjectRequest.RoadmapItem> to List<Map<String, Object>> for entity storage
     */
    default List<Map<String, Object>> convertRoadmapToMapList(List<ProjectRequest.RoadmapItem> roadmapItems) {
        if (roadmapItems == null || roadmapItems.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProjectRequest.RoadmapItem item : roadmapItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", item.getDate() != null ? item.getDate().toString() : null);
            map.put("title", item.getTitle());
            map.put("description", item.getDescription());
            result.add(map);
        }
        return result;
    }

    /**
     * Convert List<Map<String, Object>> (from entity) to List of RoadmapItem DTOs
     */
    default List<ProjectResponse.RoadmapItem> convertRoadmapToDto(List<Map<String, Object>> roadmap) {
        if (roadmap == null || roadmap.isEmpty()) {
            return null;
        }
        return roadmap.stream()
                .map(map -> {
                    ProjectResponse.RoadmapItem roadmapItem = new ProjectResponse.RoadmapItem();
                    if (map.get("date") != null) {
                        roadmapItem.setDate(java.time.LocalDate.parse(map.get("date").toString()));
                    }
                    roadmapItem.setTitle(map.get("title") != null ? map.get("title").toString() : null);
                    roadmapItem.setDescription(map.get("description") != null ? map.get("description").toString() : null);
                    return roadmapItem;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    List<ProjectResponse> toResponseList(List<Project> projects);
}
