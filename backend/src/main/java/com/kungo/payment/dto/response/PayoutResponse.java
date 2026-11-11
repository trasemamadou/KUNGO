package com.kungo.payment.dto.response;

import com.kungo.payment.domain.enums.PayoutStatus;
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
public class PayoutResponse {
    private UUID id;
    private UUID professionalId;
    private BigDecimal amount;
    private String currency;
    private PayoutStatus status;
    private String reference;
    private Instant paidAt;
}
