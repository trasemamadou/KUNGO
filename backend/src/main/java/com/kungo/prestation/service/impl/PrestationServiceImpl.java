package com.kungo.prestation.service.impl;

import com.kungo.category.Category;
import com.kungo.category.CategoryService;
import com.kungo.common.domain.MediaType;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.FileStorageService;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.prestation.domain.entity.ServiceMedia;
import com.kungo.prestation.dto.request.ServiceCreateRequest;
import com.kungo.prestation.dto.request.ServiceSearchRequest;
import com.kungo.prestation.dto.request.ServiceUpdateRequest;
import com.kungo.prestation.dto.response.ServiceMediaResponse;
import com.kungo.prestation.dto.response.ServiceResponse;
import com.kungo.prestation.dto.response.ServiceSummaryResponse;
import com.kungo.prestation.mapper.EventServiceMapper;
import com.kungo.prestation.repository.EventServiceRepository;
import com.kungo.prestation.repository.ServiceMediaRepository;
import com.kungo.prestation.service.PrestationService;
import com.kungo.prestation.specification.ServiceSpecifications;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.service.ProfessionalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrestationServiceImpl implements PrestationService {

    private static final String MEDIA_FOLDER = "service-media";

    private final EventServiceRepository eventServiceRepository;
    private final ServiceMediaRepository serviceMediaRepository;
    private final ProfessionalService professionalService;
    private final CategoryService categoryService;
    private final EventServiceMapper eventServiceMapper;
    private final FileStorageService fileStorageService;

    @Override
    public EventService getEntityById(UUID id) {
        return eventServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("service.not-found", id));
    }

    @Override
    public ServiceResponse getById(UUID id) {
        return enrich(getEntityById(id));
    }

    @Override
    public List<ServiceResponse> listByProfessional(UUID professionalId) {
        return eventServiceRepository.findByProfessionalId(professionalId).stream()
                .map(this::enrich)
                .toList();
    }

    @Override
    public Page<ServiceSummaryResponse> search(ServiceSearchRequest request, Pageable pageable) {
        return eventServiceRepository.findAll(ServiceSpecifications.from(request), pageable)
                .map(service -> {
                    ServiceSummaryResponse summary = eventServiceMapper.toSummary(service);
                    List<ServiceMedia> media = serviceMediaRepository.findByServiceIdOrderByDisplayOrderAsc(service.getId());
                    if (!media.isEmpty()) {
                        summary.setCoverImageUrl(media.get(0).getUrl());
                    }
                    return summary;
                });
    }

    @Override
    @Transactional
    public ServiceResponse create(UUID professionalId, ServiceCreateRequest request) {
        ProfessionalProfile professional = professionalService.getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(professional.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        Category category = categoryService.getEntityById(request.getCategoryId());

        EventService service = EventService.builder()
                .professional(professional)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .priceType(request.getPriceType())
                .price(Money.of(request.getPrice(), request.getCurrency()))
                .durationMinutes(request.getDurationMinutes())
                .conditions(request.getConditions())
                .build();

        EventService saved = eventServiceRepository.save(service);
        log.info("Service '{}' created for professional {}", saved.getTitle(), professionalId);
        return enrich(saved);
    }

    @Override
    @Transactional
    public ServiceResponse update(UUID id, ServiceUpdateRequest request) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");

        if (request.getCategoryId() != null) {
            service.setCategory(categoryService.getEntityById(request.getCategoryId()));
        }
        if (request.getTitle() != null) {
            service.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getPriceType() != null) {
            service.setPriceType(request.getPriceType());
        }
        if (request.getPrice() != null) {
            String currency = request.getCurrency() != null ? request.getCurrency() : service.getPrice().getCurrency();
            service.setPrice(Money.of(request.getPrice(), currency));
        }
        if (request.getDurationMinutes() != null) {
            service.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getConditions() != null) {
            service.setConditions(request.getConditions());
        }

        return enrich(eventServiceRepository.save(service));
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");
        service.publish();
        eventServiceRepository.save(service);
    }

    @Override
    @Transactional
    public void pause(UUID id) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");
        service.pause();
        eventServiceRepository.save(service);
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");
        service.archive();
        eventServiceRepository.save(service);
    }

    @Override
    @Transactional
    public ServiceMediaResponse uploadMedia(UUID id, MultipartFile file, MediaType type) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");

        String url = fileStorageService.store(file, MEDIA_FOLDER);
        int nextOrder = serviceMediaRepository.findByServiceIdOrderByDisplayOrderAsc(id).size();

        ServiceMedia media = ServiceMedia.builder()
                .service(service)
                .type(type)
                .url(url)
                .displayOrder(nextOrder)
                .build();
        return eventServiceMapper.toResponse(serviceMediaRepository.save(media));
    }

    @Override
    @Transactional
    public void removeMedia(UUID id, UUID mediaId) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");
        serviceMediaRepository.deleteByServiceIdAndId(id, mediaId);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        EventService service = getEntityById(id);
        SecurityUtils.requireOwnerOrRole(service.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");
        service.markDeleted();
        eventServiceRepository.save(service);
    }

    private ServiceResponse enrich(EventService service) {
        ServiceResponse response = eventServiceMapper.toResponse(service);
        response.setMedia(serviceMediaRepository.findByServiceIdOrderByDisplayOrderAsc(service.getId()).stream()
                .map(eventServiceMapper::toResponse).toList());
        return response;
    }
}
