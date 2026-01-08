package com.hafizbahtiar.spring.features.subscription.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a subscription.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpdateRequest {

    /**
     * New subscription plan ID (optional, for plan changes)
     */
    @Positive(message = "Plan ID must be positive")
    private Long planId;

    /**
     * Whether to cancel at period end (optional)
     */
    private Boolean cancelAtPeriodEnd;
}
