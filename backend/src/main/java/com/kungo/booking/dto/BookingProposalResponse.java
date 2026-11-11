package com.kungo.booking.dto;

import com.kungo.booking.enums.ProposalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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
public class BookingProposalResponse {
    private UUID id;
    private UUID proposedById;
    private String proposedByName;
    private boolean proposedByClient;
    private BigDecimal proposedPrice;
    private String proposedPriceCurrency;
    private LocalDateTime proposedEventDate;
    private Integer proposedDurationMinutes;
    private String proposedConditions;
    private String message;
    private ProposalStatus status;
    private Instant createdAt;
}
