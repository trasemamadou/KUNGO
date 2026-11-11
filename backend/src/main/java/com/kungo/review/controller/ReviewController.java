package com.kungo.review.controller;

import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import com.kungo.common.util.MessageUtils;
import com.kungo.review.dto.request.ReportCreateRequest;
import com.kungo.review.dto.request.ReviewCreateRequest;
import com.kungo.review.dto.request.ReviewResponseRequest;
import com.kungo.review.dto.response.ReportResponse;
import com.kungo.review.dto.response.ReviewResponse;
import com.kungo.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Avis")
public class ReviewController {

    private final ReviewService reviewService;
    private final MessageUtils messageUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Noter une prestation terminée")
    public ApiResponse<ReviewResponse> create(@Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.success(reviewService.create(request));
    }

    @PatchMapping("/{id}/respond")
    @Operation(summary = "Répondre à un avis (la personne notée)")
    public ApiResponse<ReviewResponse> respond(@PathVariable UUID id, @Valid @RequestBody ReviewResponseRequest request) {
        return ApiResponse.success(reviewService.respond(id, request));
    }

    @GetMapping("/target/{targetId}")
    @Operation(summary = "Lister les avis approuvés reçus par un utilisateur")
    public ApiResponse<PageResponse<ReviewResponse>> listForTarget(@PathVariable UUID targetId, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(reviewService.listForTarget(targetId, pageable)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Lister les avis d'une réservation")
    public ApiResponse<List<ReviewResponse>> listForBooking(@PathVariable UUID bookingId) {
        return ApiResponse.success(reviewService.listForBooking(bookingId));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATEUR')")
    @Operation(summary = "Approuver un avis (modération)")
    public ApiResponse<Void> approve(@PathVariable UUID id) {
        reviewService.approve(id);
        return ApiResponse.message(messageUtils.get("review.approved"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATEUR')")
    @Operation(summary = "Rejeter un avis (modération)")
    public ApiResponse<Void> reject(@PathVariable UUID id) {
        reviewService.reject(id);
        return ApiResponse.message(messageUtils.get("review.rejected"));
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Signaler un avis ou un utilisateur")
    public ApiResponse<ReportResponse> report(@Valid @RequestBody ReportCreateRequest request) {
        return ApiResponse.success(reviewService.report(request));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATEUR')")
    @Operation(summary = "Lister les signalements (modération)")
    public ApiResponse<PageResponse<ReportResponse>> listReports(Pageable pageable) {
        return ApiResponse.success(PageResponse.of(reviewService.listReports(pageable)));
    }

    @PatchMapping("/reports/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATEUR')")
    @Operation(summary = "Classer sans suite un signalement")
    public ApiResponse<Void> dismissReport(@PathVariable UUID id) {
        reviewService.dismissReport(id);
        return ApiResponse.message(messageUtils.get("report.dismissed"));
    }

    @PatchMapping("/reports/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATEUR')")
    @Operation(summary = "Marquer un signalement comme traité")
    public ApiResponse<Void> resolveReport(@PathVariable UUID id) {
        reviewService.resolveReport(id);
        return ApiResponse.message(messageUtils.get("report.resolved"));
    }
}
