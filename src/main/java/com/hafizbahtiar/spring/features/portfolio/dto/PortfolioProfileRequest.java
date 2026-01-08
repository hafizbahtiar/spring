package com.hafizbahtiar.spring.features.portfolio.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating or updating a portfolio profile.
 * All fields are optional to allow partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioProfileRequest {

    /**
     * User's bio or about section
     */
    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;

    /**
     * User's location (city, country, etc.)
     */
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    /**
     * Availability status (e.g., "Available", "Busy", "Not Available")
     */
    @Size(max = 50, message = "Availability status must not exceed 50 characters")
    private String availability;

    /**
     * URL to user's avatar/profile picture
     */
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    /**
     * URL to user's resume/CV
     */
    @Size(max = 500, message = "Resume URL must not exceed 500 characters")
    private String resumeUrl;

    /**
     * Social media links (LinkedIn, GitHub, Twitter, etc.)
     * Key: Platform name (e.g., "linkedin", "github", "twitter")
     * Value: URL to the profile
     */
    private Map<String, String> socialLinks;

    /**
     * User preferences (theme, language, etc.)
     * Key: Preference name (e.g., "theme", "language")
     * Value: Preference value (can be String, Boolean, Number, etc.)
     */
    private Map<String, Object> preferences;
}

