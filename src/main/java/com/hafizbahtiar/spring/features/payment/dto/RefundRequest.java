package com.hafizbahtiar.spring.features.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for processing a refund.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    /**
     * Refund amount (optional, null means full refund)
     */
    @DecimalMin(value = "0.01", message = "Refund amount must be at least 0.01")
    private BigDecimal amount;

    /**
     * Refund reason
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
