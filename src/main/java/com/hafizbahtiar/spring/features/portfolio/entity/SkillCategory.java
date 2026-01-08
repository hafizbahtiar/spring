package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing skill categories.
 */
@Getter
public enum SkillCategory {
    /**
     * Technical skills (programming languages, frameworks, tools)
     */
    TECHNICAL("TECHNICAL", "Technical"),

    /**
     * Soft skills (communication, leadership, teamwork)
     */
    SOFT("SOFT", "Soft Skills"),

    /**
     * Language skills (English, Spanish, etc.)
     */
    LANGUAGE("LANGUAGE", "Language"),

    /**
     * Certification skills
     */
    CERTIFICATION("CERTIFICATION", "Certification"),

    /**
     * Other skills
     */
    OTHER("OTHER", "Other");

    private final String value;
    private final String displayName;

    SkillCategory(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to SkillCategory enum
     */
    public static SkillCategory fromString(String text) {
        if (text == null) {
            return null;
        }
        for (SkillCategory category : SkillCategory.values()) {
            if (category.value.equalsIgnoreCase(text)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown skill category: " + text);
    }
}
