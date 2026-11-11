package com.kungo.professional.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.Set;
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
public class ProfessionalProfileUpdateRequest {

    @Size(max = 150)
    private String businessName;

    @Size(max = 3000)
    private String biography;

    @Min(0)
    @Max(80)
    private Integer yearsExperience;

    @Valid
    private AddressRequest address;

    private Set<UUID> skillIds;
    private Set<UUID> languageIds;
}
