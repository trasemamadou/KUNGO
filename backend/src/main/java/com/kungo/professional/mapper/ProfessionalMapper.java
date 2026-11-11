package com.kungo.professional.mapper;

import com.kungo.professional.domain.entity.Availability;
import com.kungo.professional.domain.entity.Language;
import com.kungo.professional.domain.entity.ProfessionalMedia;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.domain.entity.Skill;
import com.kungo.professional.domain.entity.SocialLink;
import com.kungo.professional.dto.response.AvailabilityResponse;
import com.kungo.professional.dto.response.LanguageResponse;
import com.kungo.professional.dto.response.ProfessionalMediaResponse;
import com.kungo.professional.dto.response.ProfessionalProfileResponse;
import com.kungo.professional.dto.response.ProfessionalSummaryResponse;
import com.kungo.professional.dto.response.SkillResponse;
import com.kungo.professional.dto.response.SocialLinkResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfessionalMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "media", ignore = true)
    ProfessionalProfileResponse toResponse(ProfessionalProfile profile);

    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.country", target = "country")
    ProfessionalSummaryResponse toSummary(ProfessionalProfile profile);

    SkillResponse toResponse(Skill skill);

    LanguageResponse toResponse(Language language);

    SocialLinkResponse toResponse(SocialLink socialLink);

    ProfessionalMediaResponse toResponse(ProfessionalMedia media);

    AvailabilityResponse toResponse(Availability availability);
}
