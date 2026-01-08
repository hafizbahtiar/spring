package com.hafizbahtiar.spring.features.subscription.mapper;

import com.hafizbahtiar.spring.features.subscription.dto.SubscriptionPlanResponse;
import com.hafizbahtiar.spring.features.subscription.entity.SubscriptionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for SubscriptionPlan entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionPlanMapper {

    @Mapping(target = "displayName", expression = "java(plan.getDisplayName())")
    @Mapping(target = "formattedPrice", expression = "java(plan.getFormattedPrice())")
    SubscriptionPlanResponse toResponse(SubscriptionPlan plan);

    List<SubscriptionPlanResponse> toResponseList(List<SubscriptionPlan> plans);
}
