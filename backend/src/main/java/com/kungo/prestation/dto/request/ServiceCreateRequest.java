package com.kungo.prestation.dto.request;

import com.kungo.prestation.domain.enums.PriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class ServiceCreateRequest {

    @NotNull
    private UUID categoryId;

    @NotBlank
    @Size(min = 5, max = 150)
    private String title;

    @Size(max = 3000)
    private String description;

    @NotNull
    private PriceType priceType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Builder.Default
    private String currency = "XOF";

    @Positive
    private Integer durationMinutes;

    @Size(max = 2000)
    private String conditions;
}
