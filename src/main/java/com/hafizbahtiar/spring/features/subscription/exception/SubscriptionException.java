package com.hafizbahtiar.spring.features.subscription.exception;

/**
 * Exception thrown when subscription operations fail.
 */
public class SubscriptionException extends RuntimeException {

    public SubscriptionException(String message) {
        super(message);
    }

    public SubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SubscriptionException creationFailed(String reason) {
        return new SubscriptionException("Subscription creation failed: " + reason);
    }

    public static SubscriptionException updateFailed(String reason) {
        return new SubscriptionException("Subscription update failed: " + reason);
    }

    public static SubscriptionException invalidState(String reason) {
        return new SubscriptionException("Invalid subscription state: " + reason);
    }

    public static SubscriptionException alreadyExists(Long userId) {
        return new SubscriptionException("User already has an active subscription: " + userId);
    }

    public static SubscriptionException renewalFailed(String reason) {
        return new SubscriptionException("Subscription renewal failed: " + reason);
    }

    public static SubscriptionException planHasActiveSubscriptions(Long planId, long activeCount) {
        return new SubscriptionException(
                String.format("Cannot deactivate subscription plan with ID %d: %d active subscription(s) exist",
                        planId, activeCount));
    }
}
