package com.hafizbahtiar.spring.features.payment.exception;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public static PaymentNotFoundException byId(Long id) {
        return new PaymentNotFoundException("Payment not found with ID: " + id);
    }

    public static PaymentNotFoundException byProviderId(String providerPaymentId) {
        return new PaymentNotFoundException("Payment not found with provider ID: " + providerPaymentId);
    }
}
