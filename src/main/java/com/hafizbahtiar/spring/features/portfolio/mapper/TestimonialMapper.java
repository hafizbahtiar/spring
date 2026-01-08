package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.TestimonialResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Testimonial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Testimonial entity ↔ DTO conversions.
 * Handles calculated fields (ratingStars).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TestimonialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isFeatured", expression = "java(request.getIsFeatured() != null ? request.getIsFeatured() : false)")
    @Mapping(target = "isApproved", expression = "java(request.getIsApproved() != null ? request.getIsApproved() : false)")
    Testimonial toEntity(TestimonialRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(TestimonialRequest request, @MappingTarget Testimonial testimonial);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(target = "ratingStars", expression = "java(testimonial.getRatingStars())")
    TestimonialResponse toResponse(Testimonial testimonial);

    List<TestimonialResponse> toResponseList(List<Testimonial> testimonials);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "authorName", source = "authorName")
    @Mapping(target = "authorTitle", source = "authorTitle")
    @Mapping(target = "authorCompany", source = "authorCompany")
    @Mapping(target = "authorImageUrl", source = "authorImageUrl")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "ratingStars", expression = "java(testimonial.getRatingStars())")
    @Mapping(target = "isFeatured", source = "isFeatured")
    TestimonialResponse.Summary toSummary(Testimonial testimonial);

    List<TestimonialResponse.Summary> toSummaryList(List<Testimonial> testimonials);
}
