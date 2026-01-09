package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for portfolio profile.
 * Contains user's portfolio-specific profile information including bio, location,
 * availability status, avatar, resume, social links, and preferences.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioProfileResponse {

    private Long id; // Internal ID (not exposed in public APIs)
    private UUID uuid; // Public UUID for portfolio profile identification
    private Long userId; // Internal user ID (not exposed in public APIs)
    private UUID userUuid; // Public UUID for user identification

    /**
     * User's bio or about section
     */
    private String bio;

    /**
     * User's location (city, country, etc.)
     */
    private String location;

    /**
     * Availability status (e.g., "Available", "Busy", "Not Available")
     */
    private String availability;

    /**
     * URL to user's avatar/profile picture
     */
    private String avatarUrl;

    /**
     * URL to user's resume/CV
     */
    private String resumeUrl;

    /**
     * Social media links (LinkedIn, GitHub, Twitter, etc.)
     * Key: Platform name (e.g., "linkedin", "github", "twitter")
     * Value: URL to the profile
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> socialLinks;

    /**
     * User preferences (theme, language, etc.)
     * Key: Preference name (e.g., "theme", "language")
     * Value: Preference value (can be String, Boolean, Number, etc.)
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> preferences;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

