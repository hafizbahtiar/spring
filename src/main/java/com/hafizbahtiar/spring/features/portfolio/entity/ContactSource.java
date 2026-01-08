package com.hafizbahtiar.spring.features.portfolio.entity;

/**
 * Contact source enum.
 * Represents where the contact message originated from.
 */
public enum ContactSource {
    /**
     * Contact form submission
     */
    FORM("Contact Form"),

    /**
     * Direct email
     */
    EMAIL("Email"),

    /**
     * Social media (LinkedIn, Twitter, etc.)
     */
    SOCIAL("Social Media"),

    /**
     * Other sources
     */
    OTHER("Other");

    private final String displayName;

    ContactSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
