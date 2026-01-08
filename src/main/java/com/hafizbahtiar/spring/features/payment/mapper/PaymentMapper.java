package com.hafizbahtiar.spring.features.payment.mapper;

import com.hafizbahtiar.spring.features.payment.dto.PaymentResponse;
import com.hafizbahtiar.spring.features.payment.dto.PaymentStatusResponse;
import com.hafizbahtiar.spring.features.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

/**
 * MapStruct mapper for Payment entity ↔ DTO conversions.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "userId", source = "user.id")
    PaymentResponse toResponse(Payment payment);

    PaymentStatusResponse toStatusResponse(Payment payment);

    default Page<PaymentResponse> toResponsePage(Page<Payment> paymentPage) {
        return paymentPage.map(this::toResponse);
    }
}
