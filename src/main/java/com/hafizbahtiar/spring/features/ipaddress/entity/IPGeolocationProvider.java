package com.hafizbahtiar.spring.features.ipaddress.entity;

import lombok.Getter;

/**
 * Enum representing IP geolocation providers supported by the application.
 * Currently focused on IPLocalize.com, with IPLocate.io support planned.
 */
@Getter
public enum IPGeolocationProvider {
    /**
     * IPLocalize.com - Free IP geolocation service
     * No API key required, 60 requests/minute rate limit
     */
    IPLOCALIZE("IPLOCALIZE", "IPLocalize.com"),

    /**
     * IPLocate.io - Comprehensive IP geolocation and threat intelligence
     * Requires API key, provides threat detection and hosting information
     */
    IPLOCATE("IPLOCATE", "IPLocate.io");

    private final String value;
    private final String displayName;

    IPGeolocationProvider(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to IPGeolocationProvider enum
     */
    public static IPGeolocationProvider fromString(String text) {
        if (text == null) {
            return null;
        }
        for (IPGeolocationProvider provider : IPGeolocationProvider.values()) {
            if (provider.value.equalsIgnoreCase(text)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown IP geolocation provider: " + text);
    }
}

