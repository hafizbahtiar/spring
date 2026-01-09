package com.hafizbahtiar.spring.features.portfolio.entity;

import lombok.Getter;

/**
 * Enum representing platform types for projects.
 */
@Getter
public enum PlatformType {
    /**
     * Web application (browser-based)
     */
    WEB("WEB", "Web"),

    /**
     * Android mobile application
     */
    ANDROID("ANDROID", "Android"),

    /**
     * iOS mobile application
     */
    IOS("IOS", "iOS"),

    /**
     * Desktop application (Windows, macOS, Linux)
     */
    DESKTOP("DESKTOP", "Desktop"),

    /**
     * Multi-platform (supports multiple platforms)
     */
    MULTI_PLATFORM("MULTI_PLATFORM", "Multi-Platform"),

    /**
     * Other platform type
     */
    OTHER("OTHER", "Other");

    private final String value;
    private final String displayName;

    PlatformType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to PlatformType enum
     */
    public static PlatformType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (PlatformType type : PlatformType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown platform type: " + text);
    }

    /**
     * Check if platform is mobile (Android or iOS)
     */
    public boolean isMobile() {
        return this == ANDROID || this == IOS;
    }

    /**
     * Check if platform is web-based
     */
    public boolean isWeb() {
        return this == WEB;
    }

    /**
     * Check if platform is desktop
     */
    public boolean isDesktop() {
        return this == DESKTOP;
    }
}
