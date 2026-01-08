package com.hafizbahtiar.spring.features.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a payment.
 * Stripe-focused: creates a PaymentIntent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * Payment amount (must be positive)
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    /**
     * Currency code (ISO 4217, e.g., USD, EUR)
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "USD";

    /**
     * Payment method ID (optional, for saved payment methods)
     */
    private Long paymentMethodId;

    /**
     * Order ID (optional, for order payments)
     */
    private Long orderId;

    /**
     * Subscription ID (optional, for subscription payments)
     */
    private Long subscriptionId;

    /**
     * Payment description
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Whether to save payment method for future use
     */
    private Boolean savePaymentMethod = false;
}
