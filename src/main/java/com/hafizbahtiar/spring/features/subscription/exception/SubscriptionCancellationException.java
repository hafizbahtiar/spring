package com.hafizbahtiar.spring.features.subscription.exception;

/**
 * Exception thrown when subscription cancellation fails.
 */
public class SubscriptionCancellationException extends RuntimeException {

    public SubscriptionCancellationException(String message) {
        super(message);
    }

    public SubscriptionCancellationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SubscriptionCancellationException cannotCancel(String reason) {
        return new SubscriptionCancellationException("Subscription cannot be cancelled: " + reason);
    }

    public static SubscriptionCancellationException cancellationFailed(String reason) {
        return new SubscriptionCancellationException("Subscription cancellation failed: " + reason);
    }
}
