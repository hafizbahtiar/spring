package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing project types.
 */
@Getter
public enum ProjectType {
    /**
     * Project from work/job
     */
    WORK("WORK", "Work"),

    /**
     * Side hustle business project
     */
    SIDE_HUSTLE("SIDE_HUSTLE", "Side Hustle"),

    /**
     * Main business project
     */
    BUSINESS("BUSINESS", "Business"),

    /**
     * Personal study project
     */
    STUDY_PERSONAL("STUDY_PERSONAL", "Study Personal"),

    /**
     * Hobby project
     */
    HOBBY("HOBBY", "Hobby"),

    /**
     * Other project type
     */
    OTHER("OTHER", "Other");

    private final String value;
    private final String displayName;

    ProjectType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to ProjectType enum
     */
    public static ProjectType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (ProjectType type : ProjectType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown project type: " + text);
    }

    /**
     * Check if project type is business-related (side hustle or main business)
     */
    public boolean isBusinessRelated() {
        return this == SIDE_HUSTLE || this == BUSINESS;
    }

    /**
     * Check if project type is professional (work or business)
     */
    public boolean isProfessional() {
        return this == WORK || this == BUSINESS || this == SIDE_HUSTLE;
    }

    /**
     * Check if project type is personal (study, hobby, other)
     */
    public boolean isPersonal() {
        return this == STUDY_PERSONAL || this == HOBBY || this == OTHER;
    }
}
