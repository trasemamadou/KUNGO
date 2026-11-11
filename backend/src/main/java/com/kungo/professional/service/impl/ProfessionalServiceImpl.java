package com.kungo.professional.service.impl;

import com.kungo.common.domain.Address;
import com.kungo.common.domain.MediaType;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.FileStorageService;
import com.kungo.professional.domain.entity.Availability;
import com.kungo.professional.domain.entity.Language;
import com.kungo.professional.domain.entity.ProfessionalMedia;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.domain.entity.Skill;
import com.kungo.professional.domain.entity.SocialLink;
import com.kungo.professional.dto.request.AvailabilityRequest;
import com.kungo.professional.dto.request.ProfessionalProfileUpdateRequest;
import com.kungo.professional.dto.request.ProfessionalSearchRequest;
import com.kungo.professional.dto.request.SocialLinkRequest;
import com.kungo.professional.dto.response.AvailabilityResponse;
import com.kungo.professional.dto.response.ProfessionalMediaResponse;
import com.kungo.professional.dto.response.ProfessionalProfileResponse;
import com.kungo.professional.dto.response.ProfessionalSummaryResponse;
import com.kungo.professional.dto.response.SocialLinkResponse;
import com.kungo.professional.mapper.ProfessionalMapper;
import com.kungo.professional.repository.AvailabilityRepository;
import com.kungo.professional.repository.LanguageRepository;
import com.kungo.professional.repository.ProfessionalMediaRepository;
import com.kungo.professional.repository.ProfessionalProfileRepository;
import com.kungo.professional.repository.SkillRepository;
import com.kungo.professional.repository.SocialLinkRepository;
import com.kungo.professional.service.ProfessionalService;
import com.kungo.professional.specification.ProfessionalSpecifications;
import com.kungo.user.domain.entity.User;
import com.kungo.user.event.UserRegisteredEvent;
import com.kungo.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessionalServiceImpl implements ProfessionalService {

    private static final String MEDIA_FOLDER = "professional-media";

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final SkillRepository skillRepository;
    private final LanguageRepository languageRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final ProfessionalMediaRepository professionalMediaRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final ProfessionalMapper professionalMapper;
    private final FileStorageService fileStorageService;

    @Override
    public ProfessionalProfile getEntityById(UUID id) {
        return professionalProfileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("professional.not-found", id));
    }

    @Override
    public ProfessionalProfileResponse getById(UUID id) {
        return enrich(getEntityById(id));
    }

    @Override
    public ProfessionalProfileResponse getByUserId(UUID userId) {
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("professional.not-found", userId));
        return enrich(profile);
    }

    @Override
    public ProfessionalProfileResponse getCurrentProfile() {
        return getByUserId(SecurityUtils.currentUserId());
    }

    @Override
    @Transactional
    public ProfessionalProfileResponse update(UUID id, ProfessionalProfileUpdateRequest request) {
        ProfessionalProfile profile = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");

        if (request.getBusinessName() != null) {
            profile.setBusinessName(request.getBusinessName());
        }
        if (request.getBiography() != null) {
            profile.setBiography(request.getBiography());
        }
        if (request.getYearsExperience() != null) {
            profile.setYearsExperience(request.getYearsExperience());
        }
        if (request.getAddress() != null) {
            profile.setAddress(Address.builder()
                    .country(request.getAddress().getCountry())
                    .region(request.getAddress().getRegion())
                    .city(request.getAddress().getCity())
                    .street(request.getAddress().getStreet())
                    .latitude(request.getAddress().getLatitude())
                    .longitude(request.getAddress().getLongitude())
                    .build());
        }
        if (request.getSkillIds() != null) {
            profile.setSkills(Set.copyOf(skillRepository.findByIdIn(request.getSkillIds())));
        }
        if (request.getLanguageIds() != null) {
            profile.setLanguages(Set.copyOf(languageRepository.findByIdIn(request.getLanguageIds())));
        }

        ProfessionalProfile saved = professionalProfileRepository.save(profile);
        return enrich(saved);
    }

    @Override
    public Page<ProfessionalSummaryResponse> search(ProfessionalSearchRequest request, Pageable pageable) {
        return professionalProfileRepository.findAll(ProfessionalSpecifications.from(request), pageable)
                .map(professionalMapper::toSummary);
    }

    @Override
    @Transactional
    public SocialLinkResponse addSocialLink(UUID professionalId, SocialLinkRequest request) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");

        SocialLink link = SocialLink.builder()
                .professional(profile)
                .platform(request.getPlatform())
                .url(request.getUrl())
                .build();
        return professionalMapper.toResponse(socialLinkRepository.save(link));
    }

    @Override
    @Transactional
    public void removeSocialLink(UUID professionalId, UUID linkId) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        socialLinkRepository.deleteByProfessionalIdAndId(professionalId, linkId);
    }

    @Override
    @Transactional
    public ProfessionalMediaResponse uploadMedia(UUID professionalId, MultipartFile file, MediaType type) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");

        String url = fileStorageService.store(file, MEDIA_FOLDER);
        int nextOrder = professionalMediaRepository.findByProfessionalIdOrderByDisplayOrderAsc(professionalId).size();

        ProfessionalMedia media = ProfessionalMedia.builder()
                .professional(profile)
                .type(type)
                .url(url)
                .displayOrder(nextOrder)
                .build();
        return professionalMapper.toResponse(professionalMediaRepository.save(media));
    }

    @Override
    @Transactional
    public void removeMedia(UUID professionalId, UUID mediaId) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        professionalMediaRepository.deleteByProfessionalIdAndId(professionalId, mediaId);
    }

    @Override
    public List<AvailabilityResponse> listAvailabilities(UUID professionalId) {
        return availabilityRepository.findByProfessionalId(professionalId).stream()
                .map(professionalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AvailabilityResponse addAvailability(UUID professionalId, AvailabilityRequest request) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("error.validation");
        }
        if (Boolean.TRUE.equals(request.getRecurring()) && request.getDayOfWeek() == null) {
            throw new BadRequestException("error.validation");
        }
        if (!Boolean.TRUE.equals(request.getRecurring()) && request.getSpecificDate() == null) {
            throw new BadRequestException("error.validation");
        }

        Availability availability = Availability.builder()
                .professional(profile)
                .specificDate(request.getSpecificDate())
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .recurring(Boolean.TRUE.equals(request.getRecurring()))
                .build();

        return professionalMapper.toResponse(availabilityRepository.save(availability));
    }

    @Override
    @Transactional
    public void removeAvailability(UUID professionalId, UUID availabilityId) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        availabilityRepository.deleteByProfessionalIdAndId(professionalId, availabilityId);
    }

    @Override
    @Transactional
    public void submitForVerification(UUID professionalId) {
        ProfessionalProfile profile = getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(profile.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        profile.submitForVerification();
        professionalProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void approveVerification(UUID professionalId) {
        ProfessionalProfile profile = getEntityById(professionalId);
        profile.approveVerification();
        professionalProfileRepository.save(profile);
        log.info("Professional {} verified", professionalId);
    }

    @Override
    @Transactional
    public void rejectVerification(UUID professionalId) {
        ProfessionalProfile profile = getEntityById(professionalId);
        profile.rejectVerification();
        professionalProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void activatePremium(UUID professionalId, int months) {
        ProfessionalProfile profile = getEntityById(professionalId);
        profile.activatePremium(Instant.now().plus(30L * months, ChronoUnit.DAYS));
        professionalProfileRepository.save(profile);
        log.info("Professional {} premium activated for {} months", professionalId, months);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.role() != com.kungo.user.domain.enums.Role.PROFESSIONNEL) {
            return;
        }
        if (professionalProfileRepository.existsByUserId(event.userId())) {
            return;
        }
        User user = userRepository.findById(event.userId()).orElseThrow();
        ProfessionalProfile profile = ProfessionalProfile.builder().user(user).build();
        professionalProfileRepository.save(profile);
        log.info("Empty professional profile auto-created for user {}", event.userId());
    }

    private ProfessionalProfileResponse enrich(ProfessionalProfile profile) {
        ProfessionalProfileResponse response = professionalMapper.toResponse(profile);
        response.setSocialLinks(socialLinkRepository.findByProfessionalId(profile.getId()).stream()
                .map(professionalMapper::toResponse).toList());
        response.setMedia(professionalMediaRepository.findByProfessionalIdOrderByDisplayOrderAsc(profile.getId()).stream()
                .map(professionalMapper::toResponse).toList());
        return response;
    }
}
