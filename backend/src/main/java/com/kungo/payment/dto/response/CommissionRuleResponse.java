package com.kungo.payment.dto.response;

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
public class CommissionRuleResponse {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal percentage;
    private Instant effectiveFrom;
    private boolean active;
}
