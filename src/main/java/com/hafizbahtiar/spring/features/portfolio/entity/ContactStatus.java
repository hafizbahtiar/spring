package com.hafizbahtiar.spring.features.portfolio.entity;

/**
 * Contact status enum.
 * Represents the status of a contact message or inquiry.
 */
public enum ContactStatus {
    /**
     * New contact message (not yet read)
     */
    NEW("New"),

    /**
     * Contact message has been read
     */
    READ("Read"),

    /**
     * Contact message has been replied to
     */
    REPLIED("Replied"),

    /**
     * Contact message has been archived
     */
    ARCHIVED("Archived");

    private final String displayName;

    ContactStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
