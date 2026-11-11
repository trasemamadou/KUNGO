package com.kungo.professional.controller;

import com.kungo.common.domain.MediaType;
import com.kungo.admin.service.AuditLogService;
import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import com.kungo.common.util.MessageUtils;
import com.kungo.professional.dto.request.AvailabilityRequest;
import com.kungo.professional.dto.request.ProfessionalProfileUpdateRequest;
import com.kungo.professional.dto.request.ProfessionalSearchRequest;
import com.kungo.professional.dto.request.SocialLinkRequest;
import com.kungo.professional.dto.response.AvailabilityResponse;
import com.kungo.professional.dto.response.ProfessionalMediaResponse;
import com.kungo.professional.dto.response.ProfessionalProfileResponse;
import com.kungo.professional.dto.response.ProfessionalSummaryResponse;
import com.kungo.professional.dto.response.SocialLinkResponse;
import com.kungo.professional.service.ProfessionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/professionals")
@RequiredArgsConstructor
@Tag(name = "Professionnels")
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private final MessageUtils messageUtils;
    private final AuditLogService auditLogService;

    @GetMapping("/search")
    @Operation(summary = "Recherche multicritère des professionnels")
    public ApiResponse<PageResponse<ProfessionalSummaryResponse>> search(ProfessionalSearchRequest request, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(professionalService.search(request, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le profil public d'un professionnel")
    public ApiResponse<ProfessionalProfileResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(professionalService.getById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Mon profil professionnel")
    public ApiResponse<ProfessionalProfileResponse> getCurrentProfile() {
        return ApiResponse.success(professionalService.getCurrentProfile());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour le profil (propriétaire ou admin)")
    public ApiResponse<ProfessionalProfileResponse> update(@PathVariable UUID id,
                                                             @Valid @RequestBody ProfessionalProfileUpdateRequest request) {
        return ApiResponse.success(professionalService.update(id, request));
    }

    @PostMapping("/{id}/social-links")
    @Operation(summary = "Ajouter un lien réseau social")
    public ApiResponse<SocialLinkResponse> addSocialLink(@PathVariable UUID id, @Valid @RequestBody SocialLinkRequest request) {
        return ApiResponse.success(professionalService.addSocialLink(id, request));
    }

    @DeleteMapping("/{id}/social-links/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un lien réseau social")
    public void removeSocialLink(@PathVariable UUID id, @PathVariable UUID linkId) {
        professionalService.removeSocialLink(id, linkId);
    }

    @PostMapping("/{id}/media")
    @Operation(summary = "Téléverser une photo ou vidéo de présentation")
    public ApiResponse<ProfessionalMediaResponse> uploadMedia(@PathVariable UUID id,
                                                                @RequestParam("file") MultipartFile file,
                                                                @RequestParam MediaType type) {
        return ApiResponse.success(professionalService.uploadMedia(id, file, type));
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un média")
    public void removeMedia(@PathVariable UUID id, @PathVariable UUID mediaId) {
        professionalService.removeMedia(id, mediaId);
    }

    @GetMapping("/{id}/availabilities")
    @Operation(summary = "Lister les disponibilités")
    public ApiResponse<List<AvailabilityResponse>> listAvailabilities(@PathVariable UUID id) {
        return ApiResponse.success(professionalService.listAvailabilities(id));
    }

    @PostMapping("/{id}/availabilities")
    @Operation(summary = "Ajouter un créneau de disponibilité")
    public ApiResponse<AvailabilityResponse> addAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityRequest request) {
        return ApiResponse.success(professionalService.addAvailability(id, request));
    }

    @DeleteMapping("/{id}/availabilities/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un créneau de disponibilité")
    public void removeAvailability(@PathVariable UUID id, @PathVariable UUID availabilityId) {
        professionalService.removeAvailability(id, availabilityId);
    }

    @PostMapping("/{id}/verification/submit")
    @Operation(summary = "Soumettre le compte à vérification")
    public ApiResponse<Void> submitForVerification(@PathVariable UUID id) {
        professionalService.submitForVerification(id);
        return ApiResponse.message(messageUtils.get("professional.verification-submitted"));
    }

    @PostMapping("/{id}/verification/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Approuver la vérification (admin)")
    public ApiResponse<Void> approveVerification(@PathVariable UUID id) {
        professionalService.approveVerification(id);
        auditLogService.record("ProfessionalProfile", id, "VERIFICATION_APPROVED", null);
        return ApiResponse.message(messageUtils.get("professional.verification-approved"));
    }

    @PostMapping("/{id}/verification/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Rejeter la vérification (admin)")
    public ApiResponse<Void> rejectVerification(@PathVariable UUID id) {
        professionalService.rejectVerification(id);
        auditLogService.record("ProfessionalProfile", id, "VERIFICATION_REJECTED", null);
        return ApiResponse.message(messageUtils.get("professional.verification-rejected"));
    }

    @PostMapping("/{id}/premium")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Activer l'abonnement premium (admin ou après paiement)")
    public ApiResponse<Void> activatePremium(@PathVariable UUID id, @RequestParam int months) {
        professionalService.activatePremium(id, months);
        return ApiResponse.message(messageUtils.get("professional.premium-activated"));
    }
}
