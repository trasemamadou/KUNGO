package com.kungo.booking.dto;

import com.kungo.booking.enums.BookingStatus;
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
public class BookingSearchRequest {
    private UUID clientId;
    private UUID professionalId;
    private BookingStatus status;
    private LocalDateTime eventDateFrom;
    private LocalDateTime eventDateTo;
}
