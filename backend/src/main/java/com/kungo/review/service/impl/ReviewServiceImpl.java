package com.kungo.review.service.impl;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.service.BookingService;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ConflictException;
import com.kungo.common.exception.ForbiddenException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.repository.ProfessionalProfileRepository;
import com.kungo.review.domain.entity.Report;
import com.kungo.review.domain.entity.Review;
import com.kungo.review.domain.enums.ModerationStatus;
import com.kungo.review.dto.request.ReportCreateRequest;
import com.kungo.review.dto.request.ReviewCreateRequest;
import com.kungo.review.dto.request.ReviewResponseRequest;
import com.kungo.review.dto.response.ReportResponse;
import com.kungo.review.dto.response.ReviewResponse;
import com.kungo.review.mapper.ReviewMapper;
import com.kungo.review.repository.ReportRepository;
import com.kungo.review.repository.ReviewRepository;
import com.kungo.review.service.ReviewService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse create(ReviewCreateRequest request) {
        Booking booking = bookingService.getEntityById(request.getBookingId());
        // Le versement automatique fait passer la réservation de COMPLETED à PAYOUT_SETTLED
        // quasi immédiatement après la double confirmation : les deux statuts sont éligibles à l'avis.
        if (booking.getStatus() != BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.PAYOUT_SETTLED) {
            throw new BadRequestException("booking.invalid-status-transition", booking.getStatus(), "REVIEWED");
        }

        UUID currentUserId = SecurityUtils.currentUserId();
        User author = userService.getEntityById(currentUserId);
        boolean authorIsClient = booking.getClient().getId().equals(currentUserId);
        boolean authorIsProfessional = booking.getProfessional().getUser().getId().equals(currentUserId);
        if (!authorIsClient && !authorIsProfessional) {
            throw new ForbiddenException("error.forbidden");
        }

        User target = authorIsClient ? booking.getProfessional().getUser() : booking.getClient();

        if (reviewRepository.findByBookingIdAndAuthorId(booking.getId(), currentUserId).isPresent()) {
            throw new ConflictException("error.conflict");
        }

        int overallRating = Math.round((request.getPunctuality() + request.getQuality() + request.getCommunication()
                + request.getValueForMoney() + request.getProfessionalism()) / 5f);

        Review review = Review.builder()
                .booking(booking)
                .author(author)
                .target(target)
                .rating(overallRating)
                .punctuality(request.getPunctuality())
                .quality(request.getQuality())
                .communication(request.getCommunication())
                .valueForMoney(request.getValueForMoney())
                .professionalism(request.getProfessionalism())
                .comment(request.getComment())
                .build();
        Review saved = reviewRepository.save(review);

        if (authorIsClient) {
            ProfessionalProfile professional = booking.getProfessional();
            professional.registerNewReview(overallRating);
            professionalProfileRepository.save(professional);
        }

        log.info("Review {} created by {} for booking {}", saved.getId(), currentUserId, booking.getId());
        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse respond(UUID reviewId, ReviewResponseRequest request) {
        Review review = getEntity(reviewId);
        SecurityUtils.requireOwnerOrRole(review.getTarget().getId(), "ADMIN", "SUPER_ADMIN");
        review.respond(request.getResponse());
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public Page<ReviewResponse> listForTarget(UUID targetId, Pageable pageable) {
        return reviewRepository.findByTargetIdAndModerationStatus(targetId, ModerationStatus.APPROVED, pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    public List<ReviewResponse> listForBooking(UUID bookingId) {
        return reviewRepository.findByBookingId(bookingId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void approve(UUID reviewId) {
        Review review = getEntity(reviewId);
        review.approve();
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void reject(UUID reviewId) {
        Review review = getEntity(reviewId);
        review.reject();
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public ReportResponse report(ReportCreateRequest request) {
        if (request.getReportedUserId() == null && request.getReportedReviewId() == null) {
            throw new BadRequestException("error.validation");
        }
        User reporter = userService.getEntityById(SecurityUtils.currentUserId());

        Report.ReportBuilder builder = Report.builder()
                .reporter(reporter)
                .reason(request.getReason());

        if (request.getReportedUserId() != null) {
            builder.reportedUser(userService.getEntityById(request.getReportedUserId()));
        }
        if (request.getReportedReviewId() != null) {
            builder.reportedReview(getEntity(request.getReportedReviewId()));
        }

        return reviewMapper.toResponse(reportRepository.save(builder.build()));
    }

    @Override
    public Page<ReportResponse> listReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void dismissReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.not-found", reportId));
        report.dismiss();
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void resolveReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.not-found", reportId));
        report.markActionTaken();
        reportRepository.save(report);
    }

    private Review getEntity(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("error.not-found", id));
    }
}
