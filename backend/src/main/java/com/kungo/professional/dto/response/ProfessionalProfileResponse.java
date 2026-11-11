package com.kungo.professional.dto.response;

import com.kungo.professional.domain.enums.VerificationStatus;
import java.math.BigDecimal;
import java.util.List;
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
public class ProfessionalProfileResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String businessName;
    private String biography;
    private int yearsExperience;
    private AddressResponse address;
    private Set<SkillResponse> skills;
    private Set<LanguageResponse> languages;
    private VerificationStatus verificationStatus;
    private boolean premium;
    private BigDecimal averageRating;
    private int totalReviews;
    private int totalBookings;
    private Integer responseTimeHours;
    private List<SocialLinkResponse> socialLinks;
    private List<ProfessionalMediaResponse> media;
}
