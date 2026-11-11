package com.kungo.payment.controller;

import com.kungo.common.response.ApiResponse;
import com.kungo.common.util.MessageUtils;
import com.kungo.payment.dto.request.PaymentRequest;
import com.kungo.payment.dto.response.PaymentResponse;
import com.kungo.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final MessageUtils messageUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initier le paiement d'une réservation acceptée")
    public ApiResponse<PaymentResponse> initiate(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success(paymentService.initiate(request));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Consulter le paiement d'une réservation")
    public ApiResponse<PaymentResponse> getByBooking(@PathVariable UUID bookingId) {
        return ApiResponse.success(paymentService.getByBooking(bookingId));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Rembourser un paiement (admin)")
    public ApiResponse<Void> refund(@PathVariable UUID id) {
        paymentService.refund(id);
        return ApiResponse.message(messageUtils.get("payment.refunded"));
    }
}
