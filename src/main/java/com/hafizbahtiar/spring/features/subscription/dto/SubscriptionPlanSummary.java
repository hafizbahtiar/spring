package com.hafizbahtiar.spring.features.subscription.dto;

import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Summary of subscription plan details (nested in SubscriptionResponse).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanSummary {
    private Long id;
    private String name;
    private String description;
    private SubscriptionPlanType planType;
    private BigDecimal price;
    private String currency;
    private BillingCycle billingCycle;
    private Object features;
}
