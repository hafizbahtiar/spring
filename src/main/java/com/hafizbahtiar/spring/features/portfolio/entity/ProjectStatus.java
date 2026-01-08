package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing project statuses.
 */
@Getter
public enum ProjectStatus {
    /**
     * Project is planned but not started
     */
    PLANNED("PLANNED", "Planned"),

    /**
     * Project is currently in progress
     */
    IN_PROGRESS("IN_PROGRESS", "In Progress"),

    /**
     * Project is completed
     */
    COMPLETED("COMPLETED", "Completed"),

    /**
     * Project is archived
     */
    ARCHIVED("ARCHIVED", "Archived");

    private final String value;
    private final String displayName;

    ProjectStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to ProjectStatus enum
     */
    public static ProjectStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown project status: " + text);
    }

    /**
     * Check if project is active (planned or in progress)
     */
    public boolean isActive() {
        return this == PLANNED || this == IN_PROGRESS;
    }

    /**
     * Check if project is completed
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
}
