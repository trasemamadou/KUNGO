package com.kungo.payment.service;

import com.kungo.payment.dto.request.PaymentRequest;
import com.kungo.payment.dto.response.PaymentResponse;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(PaymentRequest request);

    PaymentResponse getByBooking(UUID bookingId);

    void handleProviderSucceeded(String transactionRef);

    void handleProviderFailed(String transactionRef);

    void refund(UUID paymentId);
}
