package com.kungo.professional.service;

import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.dto.request.AvailabilityRequest;
import com.kungo.professional.dto.request.ProfessionalProfileUpdateRequest;
import com.kungo.professional.dto.request.ProfessionalSearchRequest;
import com.kungo.professional.dto.request.SocialLinkRequest;
import com.kungo.professional.dto.response.AvailabilityResponse;
import com.kungo.professional.dto.response.ProfessionalMediaResponse;
import com.kungo.professional.dto.response.ProfessionalProfileResponse;
import com.kungo.professional.dto.response.ProfessionalSummaryResponse;
import com.kungo.professional.dto.response.SocialLinkResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProfessionalService {

    ProfessionalProfile getEntityById(UUID id);

    ProfessionalProfileResponse getById(UUID id);

    ProfessionalProfileResponse getByUserId(UUID userId);

    ProfessionalProfileResponse getCurrentProfile();

    ProfessionalProfileResponse update(UUID id, ProfessionalProfileUpdateRequest request);

    Page<ProfessionalSummaryResponse> search(ProfessionalSearchRequest request, Pageable pageable);

    SocialLinkResponse addSocialLink(UUID professionalId, SocialLinkRequest request);

    void removeSocialLink(UUID professionalId, UUID linkId);

    ProfessionalMediaResponse uploadMedia(UUID professionalId, MultipartFile file, com.kungo.common.domain.MediaType type);

    void removeMedia(UUID professionalId, UUID mediaId);

    List<AvailabilityResponse> listAvailabilities(UUID professionalId);

    AvailabilityResponse addAvailability(UUID professionalId, AvailabilityRequest request);

    void removeAvailability(UUID professionalId, UUID availabilityId);

    void submitForVerification(UUID professionalId);

    void approveVerification(UUID professionalId);

    void rejectVerification(UUID professionalId);

    void activatePremium(UUID professionalId, int months);
}
