package com.kungo.professional.dto.request;

import java.math.BigDecimal;
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
public class AddressRequest {
    private String country;
    private String region;
    private String city;
    private String street;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
