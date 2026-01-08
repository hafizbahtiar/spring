package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.BlogRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.BlogResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Blog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MapStruct mapper for Blog entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BlogMapper {

    /**
     * Convert BlogRequest to Blog entity.
     * Ignores fields that should be set manually (id, user, timestamps, version,
     * publishedAt).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "tags", expression = "java(convertTagsToObject(request.getTags()))")
    Blog toEntity(BlogRequest request);

    /**
     * Update Blog entity from BlogRequest.
     * Only updates non-null fields from the request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "publishedAt", expression = "java(updatePublishedAt(blog, request.getPublished()))")
    @Mapping(target = "tags", expression = "java(request.getTags() != null ? convertTagsToObject(request.getTags()) : blog.getTags())")
    void updateEntityFromRequest(BlogRequest request, @MappingTarget Blog blog);

    /**
     * Convert Blog entity to BlogResponse.
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "tags", expression = "java(convertObjectToTags(blog.getTags()))")
    BlogResponse toResponse(Blog blog);

    List<BlogResponse> toResponseList(List<Blog> blogs);

    /**
     * Helper method to convert List to Object for JSONB storage.
     */
    default Object convertTagsToObject(List<String> tags) {
        return tags;
    }

    /**
     * Helper method to convert Object to List for DTO.
     */
    @SuppressWarnings("unchecked")
    default List<String> convertObjectToTags(Object tags) {
        if (tags == null) {
            return null;
        }
        if (tags instanceof List) {
            return (List<String>) tags;
        }
        return null;
    }

    /**
     * Update publishedAt timestamp when published status changes.
     */
    default LocalDateTime updatePublishedAt(Blog blog, Boolean published) {
        if (published == null) {
            return blog.getPublishedAt();
        }
        if (Boolean.TRUE.equals(published) && blog.getPublishedAt() == null) {
            return java.time.LocalDateTime.now();
        }
        if (Boolean.FALSE.equals(published)) {
            return null; // Clear publishedAt when unpublished
        }
        return blog.getPublishedAt(); // Keep existing publishedAt if already published
    }
}
