package com.kungo.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ReviewCreateRequest {

    @NotNull
    private UUID bookingId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer punctuality;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer quality;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer communication;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer valueForMoney;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer professionalism;

    @Size(max = 2000)
    private String comment;
}
