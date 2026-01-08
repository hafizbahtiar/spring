package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing employment types.
 */
@Getter
public enum EmploymentType {
    /**
     * Full-time employment
     */
    FULL_TIME("FULL_TIME", "Full Time"),

    /**
     * Part-time employment
     */
    PART_TIME("PART_TIME", "Part Time"),

    /**
     * Contract employment
     */
    CONTRACT("CONTRACT", "Contract"),

    /**
     * Internship
     */
    INTERNSHIP("INTERNSHIP", "Internship"),

    /**
     * Freelance
     */
    FREELANCE("FREELANCE", "Freelance"),

    /**
     * Self-employed
     */
    SELF_EMPLOYED("SELF_EMPLOYED", "Self Employed");

    private final String value;
    private final String displayName;

    EmploymentType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to EmploymentType enum
     */
    public static EmploymentType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (EmploymentType type : EmploymentType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown employment type: " + text);
    }
}
