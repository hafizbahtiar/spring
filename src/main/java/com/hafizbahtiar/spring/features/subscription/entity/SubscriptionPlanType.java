package com.hafizbahtiar.spring.features.subscription.entity;

import lombok.Getter;

/**
 * Enum representing subscription plan types.
 * Used for categorizing plans (e.g., Basic, Pro, Enterprise).
 * Plans are dynamic (stored in database), but plan types provide
 * categorization.
 */
@Getter
public enum SubscriptionPlanType {
    /**
     * Basic plan tier
     */
    BASIC("BASIC", "Basic"),

    /**
     * Pro plan tier
     */
    PRO("PRO", "Pro"),

    /**
     * Enterprise plan tier
     */
    ENTERPRISE("ENTERPRISE", "Enterprise"),

    /**
     * Custom plan (for special cases)
     */
    CUSTOM("CUSTOM", "Custom");

    private final String value;
    private final String displayName;

    SubscriptionPlanType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to SubscriptionPlanType enum
     */
    public static SubscriptionPlanType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (SubscriptionPlanType type : SubscriptionPlanType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown subscription plan type: " + text);
    }
}
