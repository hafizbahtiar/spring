package com.hafizbahtiar.spring.features.payment.exception;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentProcessingException failed(String reason) {
        return new PaymentProcessingException("Payment processing failed: " + reason);
    }

    public static PaymentProcessingException confirmationFailed(String reason) {
        return new PaymentProcessingException("Payment confirmation failed: " + reason);
    }
}
