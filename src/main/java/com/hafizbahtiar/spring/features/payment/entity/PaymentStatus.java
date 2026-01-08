package com.hafizbahtiar.spring.features.payment.entity;

import lombok.Getter;

/**
 * Enum representing payment statuses.
 * Aligned with Stripe payment statuses, compatible with PayPal.
 */
@Getter
public enum PaymentStatus {
    /**
     * Payment intent created, awaiting confirmation
     */
    PENDING("PENDING", "Payment pending"),

    /**
     * Payment is being processed
     */
    PROCESSING("PROCESSING", "Payment processing"),

    /**
     * Payment completed successfully
     */
    COMPLETED("COMPLETED", "Payment completed"),

    /**
     * Payment failed
     */
    FAILED("FAILED", "Payment failed"),

    /**
     * Payment was refunded (full or partial)
     */
    REFUNDED("REFUNDED", "Payment refunded"),

    /**
     * Payment was cancelled
     */
    CANCELLED("CANCELLED", "Payment cancelled");

    private final String value;
    private final String displayName;

    PaymentStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to PaymentStatus enum
     */
    public static PaymentStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + text);
    }

    /**
     * Check if payment is in a terminal state (cannot be changed)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return this == COMPLETED;
    }
}
