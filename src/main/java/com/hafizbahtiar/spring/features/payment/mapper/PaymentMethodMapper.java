package com.hafizbahtiar.spring.features.payment.mapper;

import com.hafizbahtiar.spring.features.payment.dto.PaymentMethodResponse;
import com.hafizbahtiar.spring.features.payment.entity.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for PaymentMethod entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMethodMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "displayName", expression = "java(paymentMethod.getDisplayName())")
    PaymentMethodResponse toResponse(PaymentMethod paymentMethod);

    List<PaymentMethodResponse> toResponseList(List<PaymentMethod> paymentMethods);
}
