package com.kungo.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class BookingCreateRequest {

    @NotNull
    private UUID serviceId;

    @Size(max = 100)
    private String eventType;

    @NotNull
    @Future
    private LocalDateTime eventDate;

    @Size(max = 500)
    private String eventAddress;

    @Positive
    private Integer estimatedDurationMinutes;

    @Min(1)
    private Integer participantCount;

    /** Si renseigné et différent du prix affiché de la prestation, ouvre directement une négociation. */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal proposedBudget;

    @Size(max = 2000)
    private String description;

    @Size(max = 1000)
    private String specialRequests;
}
