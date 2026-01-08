package com.hafizbahtiar.spring.features.user.mapper;

import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesRequest;
import com.hafizbahtiar.spring.features.user.dto.NotificationPreferencesResponse;
import com.hafizbahtiar.spring.features.user.entity.NotificationPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for NotificationPreferences entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationPreferencesMapper {

    /**
     * Convert NotificationPreferencesRequest to NotificationPreferences entity.
     * Ignores fields that should be set manually (id, user, timestamps, version).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    NotificationPreferences toEntity(NotificationPreferencesRequest request);

    /**
     * Update NotificationPreferences entity from NotificationPreferencesRequest.
     * Only updates non-null fields from the request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(NotificationPreferencesRequest request,
            @MappingTarget NotificationPreferences preferences);

    /**
     * Convert NotificationPreferences entity to NotificationPreferencesResponse.
     */
    @Mapping(source = "user.id", target = "userId")
    NotificationPreferencesResponse toResponse(NotificationPreferences preferences);
}
