package com.hafizbahtiar.spring.features.subscription.dto;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for subscription details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private Long subscriptionPlanId;
    private SubscriptionStatus status;
    private PaymentProvider provider;
    private String providerSubscriptionId;
    private String providerCustomerId;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;
    private LocalDateTime cancelledAt;
    private LocalDateTime trialStart;
    private LocalDateTime trialEnd;
    private BillingCycle billingCycle;
    private LocalDateTime nextBillingDate;
    private Object metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested plan details (optional, can be included)
    private SubscriptionPlanSummary plan;
}
