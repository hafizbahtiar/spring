package com.hafizbahtiar.spring.features.payment.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when refund processing fails.
 */
public class RefundException extends RuntimeException {

    public RefundException(String message) {
        super(message);
    }

    public RefundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RefundException notRefundable(String reason) {
        return new RefundException("Payment cannot be refunded: " + reason);
    }

    public static RefundException invalidAmount(BigDecimal requestedAmount, BigDecimal availableAmount) {
        return new RefundException(
                String.format("Invalid refund amount: requested %s, available %s", requestedAmount, availableAmount));
    }

    public static RefundException processingFailed(String reason) {
        return new RefundException("Refund processing failed: " + reason);
    }
}
