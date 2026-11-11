package com.kungo.prestation.controller;

import com.kungo.common.domain.MediaType;
import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import com.kungo.common.util.MessageUtils;
import com.kungo.prestation.dto.request.ServiceCreateRequest;
import com.kungo.prestation.dto.request.ServiceSearchRequest;
import com.kungo.prestation.dto.request.ServiceUpdateRequest;
import com.kungo.prestation.dto.response.ServiceMediaResponse;
import com.kungo.prestation.dto.response.ServiceResponse;
import com.kungo.prestation.dto.response.ServiceSummaryResponse;
import com.kungo.prestation.service.PrestationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Prestations")
public class PrestationController {

    private final PrestationService prestationService;
    private final MessageUtils messageUtils;

    @GetMapping("/search")
    @Operation(summary = "Recherche marketplace multicritère des prestations")
    public ApiResponse<PageResponse<ServiceSummaryResponse>> search(ServiceSearchRequest request, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(prestationService.search(request, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'une prestation")
    public ApiResponse<ServiceResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(prestationService.getById(id));
    }

    @GetMapping("/professional/{professionalId}")
    @Operation(summary = "Lister les prestations d'un professionnel")
    public ApiResponse<List<ServiceResponse>> listByProfessional(@PathVariable UUID professionalId) {
        return ApiResponse.success(prestationService.listByProfessional(professionalId));
    }

    @PostMapping("/professional/{professionalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une prestation (propriétaire du profil pro)")
    public ApiResponse<ServiceResponse> create(@PathVariable UUID professionalId, @Valid @RequestBody ServiceCreateRequest request) {
        return ApiResponse.success(prestationService.create(professionalId, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une prestation")
    public ApiResponse<ServiceResponse> update(@PathVariable UUID id, @Valid @RequestBody ServiceUpdateRequest request) {
        return ApiResponse.success(prestationService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publier la prestation")
    public ApiResponse<Void> publish(@PathVariable UUID id) {
        prestationService.publish(id);
        return ApiResponse.message(messageUtils.get("service.published"));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Mettre en pause la prestation")
    public ApiResponse<Void> pause(@PathVariable UUID id) {
        prestationService.pause(id);
        return ApiResponse.message(messageUtils.get("service.paused"));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archiver la prestation")
    public ApiResponse<Void> archive(@PathVariable UUID id) {
        prestationService.archive(id);
        return ApiResponse.message(messageUtils.get("service.archived"));
    }

    @PostMapping("/{id}/media")
    @Operation(summary = "Ajouter une photo ou vidéo à la prestation")
    public ApiResponse<ServiceMediaResponse> uploadMedia(@PathVariable UUID id,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam MediaType type) {
        return ApiResponse.success(prestationService.uploadMedia(id, file, type));
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un média de la prestation")
    public void removeMedia(@PathVariable UUID id, @PathVariable UUID mediaId) {
        prestationService.removeMedia(id, mediaId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer (soft-delete) une prestation")
    public void delete(@PathVariable UUID id) {
        prestationService.delete(id);
    }
}
