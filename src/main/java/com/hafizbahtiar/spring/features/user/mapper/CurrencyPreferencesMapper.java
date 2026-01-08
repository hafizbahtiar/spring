package com.hafizbahtiar.spring.features.user.mapper;

import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.CurrencyPreferencesResponse;
import com.hafizbahtiar.spring.features.user.entity.CurrencyPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for CurrencyPreferences entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyPreferencesMapper {

    /**
     * Convert CurrencyPreferencesRequest to CurrencyPreferences entity.
     * Ignores fields that should be set manually (id, user, timestamps, version).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    CurrencyPreferences toEntity(CurrencyPreferencesRequest request);

    /**
     * Update CurrencyPreferences entity from CurrencyPreferencesRequest.
     * Only updates non-null fields from the request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(CurrencyPreferencesRequest request, @MappingTarget CurrencyPreferences preferences);

    /**
     * Convert CurrencyPreferences entity to CurrencyPreferencesResponse.
     */
    @Mapping(source = "user.id", target = "userId")
    CurrencyPreferencesResponse toResponse(CurrencyPreferences preferences);
}
