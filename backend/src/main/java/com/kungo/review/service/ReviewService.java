package com.kungo.review.service;

import com.kungo.review.dto.request.ReportCreateRequest;
import com.kungo.review.dto.request.ReviewCreateRequest;
import com.kungo.review.dto.request.ReviewResponseRequest;
import com.kungo.review.dto.response.ReportResponse;
import com.kungo.review.dto.response.ReviewResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse create(ReviewCreateRequest request);

    ReviewResponse respond(UUID reviewId, ReviewResponseRequest request);

    Page<ReviewResponse> listForTarget(UUID targetId, Pageable pageable);

    List<ReviewResponse> listForBooking(UUID bookingId);

    void approve(UUID reviewId);

    void reject(UUID reviewId);

    ReportResponse report(ReportCreateRequest request);

    Page<ReportResponse> listReports(Pageable pageable);

    void dismissReport(UUID reportId);

    void resolveReport(UUID reportId);
}
