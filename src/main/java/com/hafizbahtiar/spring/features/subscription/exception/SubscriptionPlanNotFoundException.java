package com.hafizbahtiar.spring.features.subscription.exception;

/**
 * Exception thrown when a subscription plan is not found.
 */
public class SubscriptionPlanNotFoundException extends RuntimeException {

    public SubscriptionPlanNotFoundException(String message) {
        super(message);
    }

    public static SubscriptionPlanNotFoundException byId(Long id) {
        return new SubscriptionPlanNotFoundException("Subscription plan not found with ID: " + id);
    }

    public static SubscriptionPlanNotFoundException byProviderId(String providerPlanId) {
        return new SubscriptionPlanNotFoundException(
                "Subscription plan not found with provider plan ID: " + providerPlanId);
    }
}
