package com.hafizbahtiar.spring.features.user.mapper;

import com.hafizbahtiar.spring.features.user.dto.UserPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.UserPreferencesResponse;
import com.hafizbahtiar.spring.features.user.entity.UserPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for UserPreferences entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPreferencesMapper {

    /**
     * Convert UserPreferencesRequest to UserPreferences entity.
     * Ignores fields that should be set manually (id, user, timestamps, version).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserPreferences toEntity(UserPreferencesRequest request);

    /**
     * Update UserPreferences entity from UserPreferencesRequest.
     * Only updates non-null fields from the request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UserPreferencesRequest request, @MappingTarget UserPreferences preferences);

    /**
     * Convert UserPreferences entity to UserPreferencesResponse.
     */
    @Mapping(source = "user.id", target = "userId")
    UserPreferencesResponse toResponse(UserPreferences preferences);
}
