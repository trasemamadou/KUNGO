package com.kungo.admin.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalClients;
    private long totalProfessionals;
    private long totalBookings;
    private long pendingBookings;
    private long completedBookings;
    private long pendingVerifications;
    private long pendingReports;
    private long totalCategories;
    private long totalServices;
    private BigDecimal totalGrossVolume;
    private BigDecimal totalCommissionRevenue;
}
