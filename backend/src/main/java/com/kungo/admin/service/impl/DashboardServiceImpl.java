package com.kungo.admin.service.impl;

import com.kungo.admin.dto.response.DashboardStatsResponse;
import com.kungo.admin.service.DashboardService;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.repository.BookingRepository;
import com.kungo.category.CategoryRepository;
import com.kungo.payment.domain.enums.PaymentStatus;
import com.kungo.payment.repository.CommissionRepository;
import com.kungo.payment.repository.PaymentRepository;
import com.kungo.prestation.repository.EventServiceRepository;
import com.kungo.professional.domain.enums.VerificationStatus;
import com.kungo.professional.repository.ProfessionalProfileRepository;
import com.kungo.review.domain.enums.ReportStatus;
import com.kungo.review.repository.ReportRepository;
import com.kungo.user.domain.enums.Role;
import com.kungo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final BookingRepository bookingRepository;
    private final CategoryRepository categoryRepository;
    private final EventServiceRepository eventServiceRepository;
    private final ReportRepository reportRepository;
    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;

    @Override
    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .totalClients(userRepository.countByRole(Role.CLIENT))
                .totalProfessionals(userRepository.countByRole(Role.PROFESSIONNEL))
                .totalBookings(bookingRepository.count())
                .pendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING))
                .completedBookings(bookingRepository.countByStatusIn(
                        java.util.List.of(BookingStatus.COMPLETED, BookingStatus.PAYOUT_SETTLED)))
                .pendingVerifications(professionalProfileRepository.countByVerificationStatus(VerificationStatus.PENDING))
                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .totalCategories(categoryRepository.count())
                .totalServices(eventServiceRepository.count())
                .totalGrossVolume(paymentRepository.sumAmountByStatus(PaymentStatus.SUCCEEDED))
                .totalCommissionRevenue(commissionRepository.sumAllCommissions())
                .build();
    }
}
