package com.kungo.prestation.service;

import com.kungo.common.domain.MediaType;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.prestation.dto.request.ServiceCreateRequest;
import com.kungo.prestation.dto.request.ServiceSearchRequest;
import com.kungo.prestation.dto.request.ServiceUpdateRequest;
import com.kungo.prestation.dto.response.ServiceMediaResponse;
import com.kungo.prestation.dto.response.ServiceResponse;
import com.kungo.prestation.dto.response.ServiceSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PrestationService {

    EventService getEntityById(UUID id);

    ServiceResponse getById(UUID id);

    List<ServiceResponse> listByProfessional(UUID professionalId);

    Page<ServiceSummaryResponse> search(ServiceSearchRequest request, Pageable pageable);

    ServiceResponse create(UUID professionalId, ServiceCreateRequest request);

    ServiceResponse update(UUID id, ServiceUpdateRequest request);

    void publish(UUID id);

    void pause(UUID id);

    void archive(UUID id);

    ServiceMediaResponse uploadMedia(UUID id, MultipartFile file, MediaType type);

    void removeMedia(UUID id, UUID mediaId);

    void delete(UUID id);
}
