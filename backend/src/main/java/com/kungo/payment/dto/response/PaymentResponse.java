package com.kungo.payment.dto.response;

import com.kungo.payment.domain.enums.PaymentProvider;
import com.kungo.payment.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
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
public class PaymentResponse {
    private UUID id;
    private UUID bookingId;
    private PaymentProvider provider;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String transactionRef;
    private String clientSecret;
    private String invoiceUrl;
    private Instant paidAt;
}
