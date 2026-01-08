package com.hafizbahtiar.spring.features.payment.dto;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding a payment method.
 * Stripe-focused: expects Stripe PaymentMethod ID from frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {

    /**
     * Payment provider (STRIPE, PAYPAL, etc.)
     */
    @NotNull(message = "Provider is required")
    private PaymentProvider provider;

    /**
     * Provider-specific payment method ID
     * For Stripe: PaymentMethod ID (pm_xxx) from Stripe.js
     */
    @NotNull(message = "Payment method ID is required")
    private String providerMethodId;

    /**
     * Provider-specific customer ID (optional, will be created if not provided)
     * For Stripe: Customer ID (cus_xxx)
     */
    private String providerCustomerId;

    /**
     * Whether to set as default payment method
     */
    private Boolean setAsDefault = false;
}
