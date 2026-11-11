package com.kungo.favorite;

import java.math.BigDecimal;
import java.time.Instant;
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
public class FavoriteResponse {
    private UUID id;
    private UUID professionalId;
    private String professionalName;
    private String professionalAvatarUrl;
    private String businessName;
    private BigDecimal averageRating;
    private Instant addedAt;
}
