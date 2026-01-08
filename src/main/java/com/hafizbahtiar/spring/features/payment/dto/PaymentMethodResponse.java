package com.hafizbahtiar.spring.features.payment.dto;

import com.hafizbahtiar.spring.features.payment.entity.PaymentMethodType;
import com.hafizbahtiar.spring.features.payment.entity.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for payment method information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {

    private Long id;
    private Long userId;
    private PaymentProvider provider;
    private String providerMethodId; // Stripe PaymentMethod ID
    private String providerCustomerId; // Stripe Customer ID
    private PaymentMethodType type;
    private String last4;
    private String brand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
    private String displayName; // Formatted display (e.g., "Visa •••• 4242")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
