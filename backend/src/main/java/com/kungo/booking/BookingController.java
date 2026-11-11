package com.kungo.booking;

import com.kungo.booking.dto.BookingCreateRequest;
import com.kungo.booking.dto.BookingProposalRequest;
import com.kungo.booking.dto.BookingSearchRequest;
import com.kungo.booking.dto.ReasonRequest;
import com.kungo.booking.dto.BookingAttachmentResponse;
import com.kungo.booking.dto.BookingDocumentResponse;
import com.kungo.booking.dto.BookingMessageResponse;
import com.kungo.booking.dto.BookingProposalResponse;
import com.kungo.booking.dto.BookingResponse;
import com.kungo.booking.dto.BookingStatusHistoryResponse;
import com.kungo.booking.service.BookingDocumentService;
import com.kungo.booking.service.BookingMessageService;
import com.kungo.booking.service.BookingService;
import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Réservations")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;
    private final BookingDocumentService bookingDocumentService;
    private final BookingMessageService bookingMessageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une demande de réservation (client)")
    public ApiResponse<BookingResponse> create(@Valid @RequestBody BookingCreateRequest request) {
        return ApiResponse.success(bookingService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une réservation")
    public ApiResponse<BookingResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.getById(id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Historique des statuts de la réservation")
    public ApiResponse<List<BookingStatusHistoryResponse>> getHistory(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.getHistory(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Mes réservations (client authentifié)")
    public ApiResponse<List<BookingResponse>> listForCurrentClient() {
        return ApiResponse.success(bookingService.listForCurrentClient());
    }

    @GetMapping("/professional/{professionalId}")
    @Operation(summary = "Réservations reçues par un professionnel")
    public ApiResponse<List<BookingResponse>> listForProfessional(@PathVariable UUID professionalId) {
        return ApiResponse.success(bookingService.listForProfessional(professionalId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Recherche multicritère des réservations (admin)")
    public ApiResponse<PageResponse<BookingResponse>> search(BookingSearchRequest request, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(bookingService.search(request, pageable)));
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Accepter directement la demande (professionnel)")
    public ApiResponse<BookingResponse> accept(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.accept(id));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Refuser la demande (professionnel)")
    public ApiResponse<BookingResponse> reject(@PathVariable UUID id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.success(bookingService.reject(id, request));
    }

    // ---- Négociation ----

    @GetMapping("/{id}/proposals")
    @Operation(summary = "Lister les contre-propositions d'une négociation")
    public ApiResponse<List<BookingProposalResponse>> listProposals(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.listProposals(id));
    }

    @PostMapping("/{id}/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Soumettre une contre-proposition (client ou professionnel)")
    public ApiResponse<BookingProposalResponse> submitProposal(@PathVariable UUID id,
                                                                 @Valid @RequestBody BookingProposalRequest request) {
        return ApiResponse.success(bookingService.submitProposal(id, request));
    }

    @PatchMapping("/{id}/proposals/{proposalId}/accept")
    @Operation(summary = "Accepter une contre-proposition")
    public ApiResponse<BookingResponse> acceptProposal(@PathVariable UUID id, @PathVariable UUID proposalId) {
        return ApiResponse.success(bookingService.acceptProposal(id, proposalId));
    }

    @PatchMapping("/{id}/proposals/{proposalId}/reject")
    @Operation(summary = "Refuser une contre-proposition (la négociation reste ouverte)")
    public ApiResponse<BookingResponse> rejectProposal(@PathVariable UUID id, @PathVariable UUID proposalId) {
        return ApiResponse.success(bookingService.rejectProposal(id, proposalId));
    }

    // ---- Suivi de la prestation ----

    @PatchMapping("/{id}/prepare")
    @Operation(summary = "Marquer la prestation en préparation (professionnel)")
    public ApiResponse<BookingResponse> prepare(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.prepare(id));
    }

    @PatchMapping("/{id}/start-travel")
    @Operation(summary = "Marquer le professionnel en route (professionnel)")
    public ApiResponse<BookingResponse> startTravel(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.startTravel(id));
    }

    @PatchMapping("/{id}/mark-arrived")
    @Operation(summary = "Marquer le professionnel arrivé (professionnel)")
    public ApiResponse<BookingResponse> markArrived(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.markArrived(id));
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Démarrer la prestation (professionnel)")
    public ApiResponse<BookingResponse> start(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.start(id));
    }

    @PatchMapping("/{id}/confirm-completion")
    @Operation(summary = "Confirmer la fin de la prestation (client ou professionnel — double confirmation requise)")
    public ApiResponse<BookingResponse> confirmCompletion(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.confirmCompletion(id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler la réservation (client, professionnel ou admin)")
    public ApiResponse<BookingResponse> cancel(@PathVariable UUID id, @Valid @RequestBody ReasonRequest request) {
        return ApiResponse.success(bookingService.cancel(id, request));
    }

    // ---- Pièces jointes ----

    @GetMapping("/{id}/attachments")
    @Operation(summary = "Lister les pièces jointes de la demande")
    public ApiResponse<List<BookingAttachmentResponse>> listAttachments(@PathVariable UUID id) {
        return ApiResponse.success(bookingService.listAttachments(id));
    }

    @PostMapping("/{id}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une pièce jointe à la demande")
    public ApiResponse<BookingAttachmentResponse> uploadAttachment(@PathVariable UUID id,
                                                                     @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(bookingService.uploadAttachment(id, file));
    }

    // ---- Documents (contrat, reçu) ----

    @GetMapping("/{id}/documents")
    @Operation(summary = "Lister les documents générés pour la réservation (contrat, reçu)")
    public ApiResponse<List<BookingDocumentResponse>> listDocuments(@PathVariable UUID id) {
        return ApiResponse.success(bookingDocumentService.list(id));
    }

    // ---- Messagerie ----

    @GetMapping("/{id}/messages")
    @Operation(summary = "Lister les messages du fil de discussion (marque les messages reçus comme lus)")
    public ApiResponse<List<BookingMessageResponse>> listMessages(@PathVariable UUID id) {
        return ApiResponse.success(bookingMessageService.list(id));
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envoyer un message (texte et/ou pièce jointe) dans le fil de discussion")
    public ApiResponse<BookingMessageResponse> sendMessage(@PathVariable UUID id,
                                                              @RequestParam(value = "content", required = false) String content,
                                                              @RequestParam(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(bookingMessageService.send(id, content, file));
    }

    @GetMapping("/{id}/messages/unread-count")
    @Operation(summary = "Nombre de messages non lus dans le fil de discussion")
    public ApiResponse<Long> unreadMessageCount(@PathVariable UUID id) {
        return ApiResponse.success(bookingMessageService.unreadCount(id));
    }
}
