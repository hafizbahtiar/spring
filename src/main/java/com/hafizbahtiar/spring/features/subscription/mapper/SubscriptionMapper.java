package com.hafizbahtiar.spring.features.subscription.mapper;

import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanSummary;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionResponse;
import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionStatusResponse;
import com.hafizbahtiar.spring.features.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * MapStruct mapper for Subscription entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "subscriptionPlan.id", target = "subscriptionPlanId")
    @Mapping(target = "plan", expression = "java(mapPlanSummary(subscription))")
    SubscriptionResponse toResponse(Subscription subscription);

    default SubscriptionPlanSummary mapPlanSummary(Subscription subscription) {
        if (subscription.getSubscriptionPlan() == null) {
            return null;
        }
        return SubscriptionPlanSummary.builder()
                .id(subscription.getSubscriptionPlan().getId())
                .name(subscription.getSubscriptionPlan().getName())
                .description(subscription.getSubscriptionPlan().getDescription())
                .planType(subscription.getSubscriptionPlan().getPlanType())
                .price(subscription.getSubscriptionPlan().getPrice())
                .currency(subscription.getSubscriptionPlan().getCurrency())
                .billingCycle(subscription.getSubscriptionPlan().getBillingCycle())
                .features(subscription.getSubscriptionPlan().getFeatures())
                .build();
    }

    default SubscriptionStatusResponse toStatusResponse(Subscription subscription) {
        Long daysUntilRenewal = null;
        if (subscription.getNextBillingDate() != null) {
            daysUntilRenewal = Duration.between(LocalDateTime.now(), subscription.getNextBillingDate()).toDays();
        }

        return SubscriptionStatusResponse.builder()
                .subscriptionId(subscription.getId())
                .providerSubscriptionId(subscription.getProviderSubscriptionId())
                .status(subscription.getStatus())
                .nextBillingDate(subscription.getNextBillingDate())
                .daysUntilRenewal(daysUntilRenewal)
                .message("Subscription is " + subscription.getStatus().getDisplayName())
                .build();
    }

    default Page<SubscriptionResponse> toResponsePage(Page<Subscription> subscriptions) {
        return subscriptions.map(this::toResponse);
    }
}
