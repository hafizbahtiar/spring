package com.hafizbahtiar.spring.features.subscription.dto;

import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for subscription plan details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {

    private Long id;
    private String name;
    private String description;
    private SubscriptionPlanType planType;
    private BigDecimal price;
    private String currency;
    private BillingCycle billingCycle;
    private Object features;
    private Boolean isActive;
    private Integer maxUsers;
    private String maxStorage;
    private String providerPlanId;
    private Object metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Display name (e.g., "Basic Plan (Monthly)")
     */
    private String displayName;

    /**
     * Formatted price (e.g., "USD 29.99")
     */
    private String formattedPrice;
}
