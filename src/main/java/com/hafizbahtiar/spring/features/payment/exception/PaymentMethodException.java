package com.hafizbahtiar.spring.features.payment.exception;

/**
 * Exception thrown when payment method operations fail.
 * More general than PaymentMethodNotFoundException - covers validation errors,
 * attachment failures, etc.
 */
public class PaymentMethodException extends RuntimeException {

    public PaymentMethodException(String message) {
        super(message);
    }

    public PaymentMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PaymentMethodException invalid(String reason) {
        return new PaymentMethodException("Invalid payment method: " + reason);
    }

    public static PaymentMethodException attachmentFailed(String reason) {
        return new PaymentMethodException("Failed to attach payment method: " + reason);
    }

    public static PaymentMethodException removalFailed(String reason) {
        return new PaymentMethodException("Failed to remove payment method: " + reason);
    }

    public static PaymentMethodException alreadyExists(String providerMethodId) {
        return new PaymentMethodException("Payment method already exists: " + providerMethodId);
    }

    public static PaymentMethodException notOwnedByUser() {
        return new PaymentMethodException("Payment method does not belong to user");
    }
}
