package com.hafizbahtiar.spring.features.payment.dto;

import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for refund information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {

    private Long paymentId;
    private String providerRefundId; // Stripe Refund ID
    private BigDecimal refundAmount;
    private PaymentStatus paymentStatus;
    private String reason;
    private LocalDateTime refundedAt;
}
