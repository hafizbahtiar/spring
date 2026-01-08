package com.hafizbahtiar.spring.features.subscription.dto;

import com.hafizbahtiar.spring.features.subscription.entity.BillingCycle;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating a subscription plan (admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanRequest {

    /**
     * Plan name
     */
    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name must not exceed 100 characters")
    private String name;

    /**
     * Plan description
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Plan type (BASIC, PRO, ENTERPRISE, CUSTOM)
     */
    @NotNull(message = "Plan type is required")
    private SubscriptionPlanType planType;

    /**
     * Plan price per billing cycle
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;

    /**
     * Currency code (ISO 4217, e.g., USD, EUR)
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "USD";

    /**
     * Billing cycle (MONTHLY, QUARTERLY, YEARLY)
     */
    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    /**
     * Plan features stored as JSON (optional)
     * Example: {"maxUsers": 10, "maxStorage": "100GB", "apiCalls": 10000}
     */
    private Object features;

    /**
     * Maximum users allowed (optional)
     */
    private Integer maxUsers;

    /**
     * Maximum storage allowed (optional)
     */
    private String maxStorage;

    /**
     * Stripe Price ID (optional, will be created automatically if not provided)
     */
    private String providerPlanId;

    /**
     * Whether plan is active (default: true)
     */
    private Boolean isActive = true;
}
