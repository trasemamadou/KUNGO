package com.kungo.admin.controller;

import com.kungo.admin.dto.response.AuditLogResponse;
import com.kungo.admin.dto.response.DashboardStatsResponse;
import com.kungo.admin.mapper.AdminMapper;
import com.kungo.admin.repository.AuditLogRepository;
import com.kungo.admin.service.DashboardService;
import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tableau de bord administrateur : statistiques globales de la plateforme et
 * consultation du journal d'audit. Tous les autres écrans d'administration
 * (utilisateurs, professionnels, commissions, signalements) sont exposés par
 * leurs modules respectifs afin d'éviter la duplication de logique métier.
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Administration")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final AuditLogRepository auditLogRepository;
    private final AdminMapper adminMapper;

    @GetMapping("/stats")
    @Operation(summary = "Statistiques globales de la plateforme")
    public ApiResponse<DashboardStatsResponse> stats() {
        return ApiResponse.success(dashboardService.getStats());
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Consulter le journal d'audit")
    public ApiResponse<PageResponse<AuditLogResponse>> auditLogs(Pageable pageable) {
        return ApiResponse.success(PageResponse.of(auditLogRepository.findAll(pageable).map(adminMapper::toResponse)));
    }
}
