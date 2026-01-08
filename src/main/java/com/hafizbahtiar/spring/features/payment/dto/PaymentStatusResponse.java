package com.hafizbahtiar.spring.features.payment.dto;

import com.hafizbahtiar.spring.features.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for payment status check.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {

    private Long id;
    private String providerPaymentId;
    private PaymentStatus status;
    private String failureReason;
    private String failureCode;
    private LocalDateTime updatedAt;
}
