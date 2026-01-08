package com.hafizbahtiar.spring.features.subscription.entity;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

/**
 * Enum representing billing cycles for subscriptions.
 */
@Getter
public enum BillingCycle {
    /**
     * Monthly billing (renews every month)
     */
    MONTHLY("MONTHLY", "Monthly", 1, ChronoUnit.MONTHS),

    /**
     * Quarterly billing (renews every 3 months)
     */
    QUARTERLY("QUARTERLY", "Quarterly", 3, ChronoUnit.MONTHS),

    /**
     * Yearly billing (renews every year)
     */
    YEARLY("YEARLY", "Yearly", 12, ChronoUnit.MONTHS);

    private final String value;
    private final String displayName;
    private final int months;
    private final ChronoUnit unit;

    BillingCycle(String value, String displayName, int months, ChronoUnit unit) {
        this.value = value;
        this.displayName = displayName;
        this.months = months;
        this.unit = unit;
    }

    /**
     * Convert string to BillingCycle enum
     */
    public static BillingCycle fromString(String text) {
        if (text == null) {
            return null;
        }
        for (BillingCycle cycle : BillingCycle.values()) {
            if (cycle.value.equalsIgnoreCase(text)) {
                return cycle;
            }
        }
        throw new IllegalArgumentException("Unknown billing cycle: " + text);
    }

    /**
     * Get the number of months in this billing cycle
     */
    public int getMonths() {
        return months;
    }
}
