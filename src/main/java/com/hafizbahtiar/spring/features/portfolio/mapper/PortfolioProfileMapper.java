package com.hafizbahtiar.spring.features.portfolio.mapper;

import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileRequest;
import com.hafizbahtiar.spring.features.portfolio.dto.PortfolioProfileResponse;
import com.hafizbahtiar.spring.features.portfolio.entity.PortfolioProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

/**
 * MapStruct mapper for PortfolioProfile entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PortfolioProfileMapper {

    /**
     * Convert PortfolioProfileRequest to PortfolioProfile entity.
     * Ignores fields that should be set manually (id, user, timestamps, version).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "socialLinks", expression = "java(convertSocialLinksToObject(request.getSocialLinks()))")
    @Mapping(target = "preferences", expression = "java(convertPreferencesToObject(request.getPreferences()))")
    PortfolioProfile toEntity(PortfolioProfileRequest request);

    /**
     * Update PortfolioProfile entity from PortfolioProfileRequest.
     * Only updates non-null fields from the request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "socialLinks", expression = "java(request.getSocialLinks() != null ? convertSocialLinksToObject(request.getSocialLinks()) : profile.getSocialLinks())")
    @Mapping(target = "preferences", ignore = true) // Handle manually in @AfterMapping
    void updateEntityFromRequest(PortfolioProfileRequest request, @MappingTarget PortfolioProfile profile);

    /**
     * After mapping, conditionally update preferences only if provided in request.
     * This avoids Hibernate serialization issues when preferences is null.
     * IMPORTANT: Only updates preferences if explicitly provided in request.
     */
    @AfterMapping
    default void updatePreferencesIfProvided(PortfolioProfileRequest request, @MappingTarget PortfolioProfile profile) {
        // Only update preferences if explicitly provided in request (not null and not empty)
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            // mergePreferences already returns a new HashMap, safe to set directly
            Object merged = mergePreferences(request.getPreferences(), profile.getPreferences());
            profile.setPreferences(merged);
        }
        // If preferences is null or empty in request, don't touch existing preferences at all
    }

    /**
     * Convert PortfolioProfile entity to PortfolioProfileResponse.
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "socialLinks", expression = "java(convertObjectToSocialLinks(profile.getSocialLinks()))")
    @Mapping(target = "preferences", expression = "java(convertObjectToPreferences(profile.getPreferences()))")
    PortfolioProfileResponse toResponse(PortfolioProfile profile);

    /**
     * Helper method to convert Map to Object for JSONB storage.
     */
    default Object convertSocialLinksToObject(Map<String, String> socialLinks) {
        return socialLinks;
    }

    /**
     * Helper method to convert Map to Object for JSONB storage.
     */
    default Object convertPreferencesToObject(Map<String, Object> preferences) {
        return preferences;
    }

    /**
     * Helper method to merge preferences from request with existing preferences.
     * Merges the new preferences with existing ones to avoid losing data.
     * Only updates preferences if newPreferences is not null and not empty.
     * Always creates a new HashMap to avoid Hibernate serialization issues.
     */
    @SuppressWarnings("unchecked")
    default Object mergePreferences(Map<String, Object> newPreferences, Object existingPreferences) {
        // If newPreferences is null or empty, keep existing preferences unchanged
        if (newPreferences == null || newPreferences.isEmpty()) {
            return existingPreferences;
        }

        // Always create a new HashMap to avoid Hibernate serialization issues
        // Hibernate has trouble serializing LinkedHashMap and other Map implementations
        java.util.HashMap<String, Object> merged = new java.util.HashMap<>();

        // If existing preferences is a Map, add them first
        if (existingPreferences instanceof Map) {
            Map<String, Object> existingMap = (Map<String, Object>) existingPreferences;
            merged.putAll(existingMap);
        }

        // Then add/override with new preferences
        merged.putAll(newPreferences);

        return merged; // Return new HashMap, Hibernate can serialize this properly
    }

    /**
     * Helper method to convert Object to Map for DTO.
     */
    @SuppressWarnings("unchecked")
    default Map<String, String> convertObjectToSocialLinks(Object socialLinks) {
        if (socialLinks == null) {
            return null;
        }
        if (socialLinks instanceof Map) {
            return (Map<String, String>) socialLinks;
        }
        return null;
    }

    /**
     * Helper method to convert Object to Map for DTO.
     */
    @SuppressWarnings("unchecked")
    default Map<String, Object> convertObjectToPreferences(Object preferences) {
        if (preferences == null) {
            return null;
        }
        if (preferences instanceof Map) {
            return (Map<String, Object>) preferences;
        }
        return null;
    }
}
