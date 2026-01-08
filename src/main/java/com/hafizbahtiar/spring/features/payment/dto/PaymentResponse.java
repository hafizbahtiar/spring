package com.hafizbahtiar.spring.features.payment.dto;

import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment information.
 * Stripe-focused: includes PaymentIntent client secret for frontend
 * confirmation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long userId;
    private Long orderId;
    private Long subscriptionId;
    private PaymentProvider provider;
    private String providerPaymentId; // Stripe PaymentIntent ID
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private Long paymentMethodId;
    private String clientSecret; // Stripe PaymentIntent client secret (for frontend)
    private String failureReason;
    private String failureCode;
    private BigDecimal refundAmount;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Redirect URL (for providers that require redirect, e.g., PayPal)
     * Null for Stripe (uses client secret instead)
     */
    private String redirectUrl;
}
