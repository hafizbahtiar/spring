package com.hafizbahtiar.spring.features.subscription.exception;

/**
 * Exception thrown when a subscription is not found.
 */
public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(String message) {
        super(message);
    }

    public static SubscriptionNotFoundException byId(Long id) {
        return new SubscriptionNotFoundException("Subscription not found with ID: " + id);
    }

    public static SubscriptionNotFoundException byProviderId(String providerSubscriptionId) {
        return new SubscriptionNotFoundException(
                "Subscription not found with provider subscription ID: " + providerSubscriptionId);
    }

    public static SubscriptionNotFoundException activeForUser(Long userId) {
        return new SubscriptionNotFoundException("No active subscription found for user ID: " + userId);
    }
}
