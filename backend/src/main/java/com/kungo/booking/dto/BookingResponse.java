package com.kungo.booking.dto;

import com.kungo.booking.enums.BookingStatus;
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
public class BookingResponse {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private String clientAvatarUrl;
    private UUID professionalId;
    private String professionalName;
    private String professionalAvatarUrl;
    private UUID serviceId;
    private String serviceTitle;
    private String eventType;
    private LocalDateTime eventDate;
    private String eventAddress;
    private Integer estimatedDurationMinutes;
    private Integer participantCount;
    private BookingStatus status;
    private String description;
    private String specialRequests;
    private String professionalMessage;
    private BigDecimal totalPrice;
    private String currency;
    private BigDecimal commissionAmount;
    private BigDecimal professionalAmount;
    private Instant clientConfirmedAt;
    private Instant professionalConfirmedAt;
    private Instant createdAt;
}
