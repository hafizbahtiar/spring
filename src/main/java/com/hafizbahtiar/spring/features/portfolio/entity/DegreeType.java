package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing degree types.
 */
@Getter
public enum DegreeType {
    /**
     * Bachelor's degree
     */
    BACHELOR("BACHELOR", "Bachelor's Degree"),

    /**
     * Master's degree
     */
    MASTER("MASTER", "Master's Degree"),

    /**
     * Doctorate degree
     */
    DOCTORATE("DOCTORATE", "Doctorate"),

    /**
     * Certificate
     */
    CERTIFICATE("CERTIFICATE", "Certificate"),

    /**
     * Diploma
     */
    DIPLOMA("DIPLOMA", "Diploma"),

    /**
     * Associate degree
     */
    ASSOCIATE("ASSOCIATE", "Associate Degree"),

    /**
     * Other
     */
    OTHER("OTHER", "Other");

    private final String value;
    private final String displayName;

    DegreeType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to DegreeType enum
     */
    public static DegreeType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (DegreeType type : DegreeType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown degree type: " + text);
    }

    /**
     * Check if degree is a higher education degree
     */
    public boolean isHigherEducation() {
        return this == BACHELOR || this == MASTER || this == DOCTORATE;
    }
}
