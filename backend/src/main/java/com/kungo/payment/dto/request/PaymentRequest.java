package com.kungo.payment.dto.request;

import com.kungo.payment.domain.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private UUID bookingId;

    @NotNull
    private PaymentProvider provider;
}
