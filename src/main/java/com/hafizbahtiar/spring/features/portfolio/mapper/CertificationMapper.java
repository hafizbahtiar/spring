package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.CertificationRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CertificationResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Certification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Certification entity ↔ DTO conversions.
 * Handles calculated fields (daysUntilExpiry, isExpiringSoon).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CertificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isExpired", ignore = true) // Will be calculated by entity
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isVerified", expression = "java(request.getIsVerified() != null ? request.getIsVerified() : false)")
    Certification toEntity(CertificationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isExpired", ignore = true) // Will be calculated by entity
    void updateEntityFromRequest(CertificationRequest request, @MappingTarget Certification certification);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "daysUntilExpiry", expression = "java(certification.daysUntilExpiry())")
    @Mapping(target = "isExpiringSoon", expression = "java(certification.isExpiringSoon())")
    CertificationResponse toResponse(Certification certification);

    List<CertificationResponse> toResponseList(List<Certification> certifications);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "issuer", source = "issuer")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "isExpired", source = "isExpired")
    @Mapping(target = "isVerified", source = "isVerified")
    CertificationResponse.Summary toSummary(Certification certification);

    List<CertificationResponse.Summary> toSummaryList(List<Certification> certifications);
}
