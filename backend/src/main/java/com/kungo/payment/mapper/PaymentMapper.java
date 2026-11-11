package com.kungo.payment.mapper;

import com.kungo.payment.domain.entity.CommissionRule;
import com.kungo.payment.domain.entity.Payment;
import com.kungo.payment.domain.entity.Payout;
import com.kungo.payment.dto.response.CommissionRuleResponse;
import com.kungo.payment.dto.response.PaymentResponse;
import com.kungo.payment.dto.response.PayoutResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "amount.amount", target = "amount")
    @Mapping(source = "amount.currency", target = "currency")
    @Mapping(source = "providerClientSecret", target = "clientSecret")
    PaymentResponse toResponse(Payment payment);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    CommissionRuleResponse toResponse(CommissionRule rule);

    @Mapping(source = "professional.id", target = "professionalId")
    @Mapping(source = "amount.amount", target = "amount")
    @Mapping(source = "amount.currency", target = "currency")
    PayoutResponse toResponse(Payout payout);
}
