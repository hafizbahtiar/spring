package com.hafizbahtiar.spring.features.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating user preferences.
 * All fields are optional to allow partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesRequest {

    /**
     * Theme preference (light, dark, system)
     */
    @Pattern(regexp = "^(light|dark|system)$", message = "Theme must be one of: light, dark, system")
    @Size(max = 20, message = "Theme must not exceed 20 characters")
    private String theme;

    /**
     * Language preference (e.g., "en", "ms", "zh")
     */
    @Size(min = 2, max = 10, message = "Language must be between 2 and 10 characters")
    private String language;

    /**
     * Date format preference (e.g., "MM/DD/YYYY", "DD/MM/YYYY", "YYYY-MM-DD")
     */
    @Size(max = 20, message = "Date format must not exceed 20 characters")
    private String dateFormat;

    /**
     * Time format preference (12h or 24h)
     */
    @Pattern(regexp = "^(12h|24h)$", message = "Time format must be either 12h or 24h")
    @Size(max = 5, message = "Time format must not exceed 5 characters")
    private String timeFormat;

    /**
     * Timezone preference (e.g., "UTC", "Asia/Kuala_Lumpur", "America/New_York")
     */
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    /**
     * Default dashboard view (grid, list, table)
     */
    @Pattern(regexp = "^(grid|list|table)$", message = "Default dashboard view must be one of: grid, list, table")
    @Size(max = 20, message = "Default dashboard view must not exceed 20 characters")
    private String defaultDashboardView;

    /**
     * Items per page preference (as string to allow flexibility)
     */
    @Size(max = 10, message = "Items per page must not exceed 10 characters")
    private String itemsPerPage;

    /**
     * Show widgets on dashboard
     */
    private Boolean showWidgets;

    /**
     * Editor theme preference (light, dark, monokai, github)
     */
    @Pattern(regexp = "^(light|dark|monokai|github)$", message = "Editor theme must be one of: light, dark, monokai, github")
    @Size(max = 20, message = "Editor theme must not exceed 20 characters")
    private String editorTheme;

    /**
     * Editor font size (10-24)
     */
    @Min(value = 10, message = "Editor font size must be at least 10")
    @Max(value = 24, message = "Editor font size must not exceed 24")
    private Integer editorFontSize;

    /**
     * Editor line height (1.0-3.0)
     */
    @Min(value = 1, message = "Editor line height must be at least 1.0")
    @Max(value = 3, message = "Editor line height must not exceed 3.0")
    private Double editorLineHeight;

    /**
     * Editor tab size (2-8)
     */
    @Min(value = 2, message = "Editor tab size must be at least 2")
    @Max(value = 8, message = "Editor tab size must not exceed 8")
    private Integer editorTabSize;
}

