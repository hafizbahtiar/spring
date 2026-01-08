package com.hafizbahtiar.spring.features.payment.exception;

/**
 * Exception thrown when a payment method is not found.
 */
public class PaymentMethodNotFoundException extends RuntimeException {

    public PaymentMethodNotFoundException(String message) {
        super(message);
    }

    public static PaymentMethodNotFoundException byId(Long id) {
        return new PaymentMethodNotFoundException("Payment method not found with ID: " + id);
    }

    public static PaymentMethodNotFoundException byProviderId(String providerMethodId) {
        return new PaymentMethodNotFoundException("Payment method not found with provider ID: " + providerMethodId);
    }
}
