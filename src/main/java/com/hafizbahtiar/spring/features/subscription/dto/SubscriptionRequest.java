package com.hafizbahtiar.spring.features.subscription.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a subscription.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    /**
     * Subscription plan ID (required)
     */
    @NotNull(message = "Plan ID is required")
    private Long planId;

    /**
     * Payment method ID (optional, for saved payment methods)
     */
    private Long paymentMethodId;

    /**
     * Trial days (optional, 0-30 days)
     */
    @Positive(message = "Trial days must be positive")
    private Integer trialDays;

    /**
     * Whether to cancel at period end (default: false)
     */
    private Boolean cancelAtPeriodEnd = false;
}
