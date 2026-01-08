package com.hafizbahtiar.spring.features.payment.entity;

import lombok.Getter;

/**
 * Enum representing payment method types.
 * Stripe-focused types, compatible with other providers.
 */
@Getter
public enum PaymentMethodType {
    /**
     * Credit card payment method
     */
    CREDIT_CARD("CREDIT_CARD", "Credit Card"),

    /**
     * Debit card payment method
     */
    DEBIT_CARD("DEBIT_CARD", "Debit Card"),

    /**
     * PayPal account payment method
     */
    PAYPAL_ACCOUNT("PAYPAL_ACCOUNT", "PayPal Account"),

    /**
     * Bank transfer payment method
     */
    BANK_TRANSFER("BANK_TRANSFER", "Bank Transfer"),

    /**
     * Stripe-specific: Payment method attached to customer
     */
    STRIPE_PAYMENT_METHOD("STRIPE_PAYMENT_METHOD", "Stripe Payment Method");

    private final String value;
    private final String displayName;

    PaymentMethodType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Convert string to PaymentMethodType enum
     */
    public static PaymentMethodType fromString(String text) {
        if (text == null) {
            return null;
        }
        for (PaymentMethodType type : PaymentMethodType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown payment method type: " + text);
    }
}
