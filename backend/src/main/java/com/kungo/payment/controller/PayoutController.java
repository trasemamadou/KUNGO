package com.kungo.payment.controller;

import com.kungo.common.response.ApiResponse;
import com.kungo.payment.dto.response.PayoutResponse;
import com.kungo.payment.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payouts")
@RequiredArgsConstructor
@Tag(name = "Reversements")
public class PayoutController {

    private final PayoutService payoutService;

    @GetMapping("/professional/{professionalId}")
    @Operation(summary = "Lister les reversements d'un professionnel")
    public ApiResponse<List<PayoutResponse>> listForProfessional(@PathVariable UUID professionalId) {
        return ApiResponse.success(payoutService.listForProfessional(professionalId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un reversement (admin)")
    public ApiResponse<PayoutResponse> create(@RequestParam UUID professionalId,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam(defaultValue = "XOF") String currency) {
        return ApiResponse.success(payoutService.create(professionalId, amount, currency));
    }

    @PatchMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Marquer le reversement comme payé (admin)")
    public ApiResponse<PayoutResponse> markPaid(@PathVariable UUID id, @RequestParam String reference) {
        return ApiResponse.success(payoutService.markPaid(id, reference));
    }

    @PatchMapping("/{id}/mark-failed")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Marquer le reversement comme échoué (admin)")
    public ApiResponse<PayoutResponse> markFailed(@PathVariable UUID id) {
        return ApiResponse.success(payoutService.markFailed(id));
    }
}
