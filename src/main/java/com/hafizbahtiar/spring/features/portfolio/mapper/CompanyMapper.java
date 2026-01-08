package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.CompanyRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.CompanyResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Company entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    @Mapping(target = "isVerified", expression = "java(request.getIsVerified() != null ? request.getIsVerified() : false)")
    Company toEntity(CompanyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(CompanyRequest request, @MappingTarget Company company);

    @Mapping(source = "user.id", target = "userId")
    CompanyResponse toResponse(Company company);

    List<CompanyResponse> toResponseList(List<Company> companies);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "logoUrl", source = "logoUrl")
    @Mapping(target = "industry", source = "industry")
    @Mapping(target = "isVerified", source = "isVerified")
    CompanyResponse.Summary toSummary(Company company);

    List<CompanyResponse.Summary> toSummaryList(List<Company> companies);
}

