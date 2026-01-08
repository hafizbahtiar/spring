package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing proficiency levels for skills.
 */
@Getter
public enum ProficiencyLevel {
    /**
     * Beginner level
     */
    BEGINNER("BEGINNER", "Beginner"),

    /**
     * Intermediate level
     */
    INTERMEDIATE("INTERMEDIATE", "Intermediate"),

    /**
     * Advanced level
     */
    ADVANCED("ADVANCED", "Advanced"),

    /**
     * Expert level
     */
    EXPERT("EXPERT", "Expert");

    private final String value;
    private final String displayName;

    ProficiencyLevel(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to ProficiencyLevel enum
     */
    public static ProficiencyLevel fromString(String text) {
        if (text == null) {
            return null;
        }
        for (ProficiencyLevel level : ProficiencyLevel.values()) {
            if (level.value.equalsIgnoreCase(text)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown proficiency level: " + text);
    }

    /**
     * Check if proficiency level is at least intermediate
     */
    public boolean isAtLeastIntermediate() {
        return this == INTERMEDIATE || this == ADVANCED || this == EXPERT;
    }

    /**
     * Check if proficiency level is advanced or expert
     */
    public boolean isAdvancedOrExpert() {
        return this == ADVANCED || this == EXPERT;
    }
}
