package com.kungo.booking.dto;

import com.kungo.booking.enums.BookingStatus;
import java.time.Instant;
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
public class BookingStatusHistoryResponse {
    private BookingStatus fromStatus;
    private BookingStatus toStatus;
    private String comment;
    private Instant changedAt;
}
