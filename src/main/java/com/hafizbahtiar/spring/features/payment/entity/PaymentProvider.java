package com.hafizbahtiar.spring.features.payment.entity;

import lombok.Getter;

/**
 * Enum representing payment providers supported by the application.
 * Currently focused on Stripe, with PayPal support planned.
 */
@Getter
public enum PaymentProvider {
    /**
     * Stripe payment gateway
     * Primary focus for initial implementation
     */
    STRIPE("STRIPE", "Stripe"),

    /**
     * PayPal payment gateway
     * To be implemented after Stripe
     */
    PAYPAL("PAYPAL", "PayPal"),

    /**
     * Bank transfer (manual payment method)
     */
    BANK_TRANSFER("BANK_TRANSFER", "Bank Transfer");

    private final String value;
    private final String displayName;

    PaymentProvider(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to PaymentProvider enum
     */
    public static PaymentProvider fromString(String text) {
        if (text == null) {
            return null;
        }
        for (PaymentProvider provider : PaymentProvider.values()) {
            if (provider.value.equalsIgnoreCase(text)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown payment provider: " + text);
    }
}
