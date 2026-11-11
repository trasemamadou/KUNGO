package com.kungo.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contre-proposition dans une négociation. Tous les champs sont optionnels :
 * seuls ceux renseignés modifient les termes courants de la réservation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingProposalRequest {

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Future
    private LocalDateTime eventDate;

    @Positive
    private Integer durationMinutes;

    @Size(max = 2000)
    private String conditions;

    @Size(max = 1000)
    private String message;
}
