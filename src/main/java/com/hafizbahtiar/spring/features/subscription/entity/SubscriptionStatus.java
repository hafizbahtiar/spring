package com.hafizbahtiar.spring.features.subscription.entity;

import lombok.Getter;

/**
 * Enum representing subscription statuses.
 * Aligned with Stripe subscription statuses.
 */
@Getter
public enum SubscriptionStatus {
    /**
     * Subscription is active and in good standing
     */
    ACTIVE("ACTIVE", "Active"),

    /**
     * Subscription is in trial period
     */
    TRIALING("TRIALING", "Trialing"),

    /**
     * Payment failed, subscription is past due
     */
    PAST_DUE("PAST_DUE", "Past Due"),

    /**
     * Subscription has been cancelled
     */
    CANCELLED("CANCELLED", "Cancelled"),

    /**
     * Subscription has expired (period ended after cancellation)
     */
    EXPIRED("EXPIRED", "Expired"),

    /**
     * Subscription is incomplete (payment method needs to be added)
     */
    INCOMPLETE("INCOMPLETE", "Incomplete"),

    /**
     * Subscription was incomplete and has expired
     */
    INCOMPLETE_EXPIRED("INCOMPLETE_EXPIRED", "Incomplete Expired"),

    /**
     * Subscription is unpaid (payment failed, no retry)
     */
    UNPAID("UNPAID", "Unpaid");

    private final String value;
    private final String displayName;

    SubscriptionStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to SubscriptionStatus enum
     */
    public static SubscriptionStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown subscription status: " + text);
    }

    /**
     * Check if subscription is active (can use service)
     */
    public boolean isActive() {
        return this == ACTIVE || this == TRIALING;
    }

    /**
     * Check if subscription is in a terminal state (cannot be reactivated)
     */
    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED || this == INCOMPLETE_EXPIRED || this == UNPAID;
    }

    /**
     * Check if subscription can be cancelled
     */
    public boolean canBeCancelled() {
        return !isTerminal();
    }

    /**
     * Check if subscription needs payment attention
     */
    public boolean needsPaymentAttention() {
        return this == PAST_DUE || this == INCOMPLETE || this == UNPAID;
    }
}
