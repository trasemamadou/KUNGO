package com.kungo.prestation.dto.response;

import com.kungo.category.CategorySummaryResponse;
import com.kungo.prestation.domain.enums.PriceType;
import com.kungo.prestation.domain.enums.ServiceStatus;
import com.kungo.professional.dto.response.ProfessionalSummaryResponse;
import java.math.BigDecimal;
import java.util.List;
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
public class ServiceResponse {
    private UUID id;
    private String title;
    private String description;
    private PriceType priceType;
    private BigDecimal price;
    private String currency;
    private Integer durationMinutes;
    private String conditions;
    private ServiceStatus status;
    private CategorySummaryResponse category;
    private ProfessionalSummaryResponse professional;
    private List<ServiceMediaResponse> media;
}
