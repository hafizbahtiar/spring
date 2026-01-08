package com.hafizbahtiar.spring.features.subscription.dto;

import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for subscription status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {

    private Long subscriptionId;
    private String providerSubscriptionId;
    private SubscriptionStatus status;
    private LocalDateTime nextBillingDate;
    private Long daysUntilRenewal;
    private String message;
}
