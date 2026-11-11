package com.kungo.payment.controller;

import com.kungo.admin.service.AuditLogService;
import com.kungo.category.Category;
import com.kungo.category.CategoryService;
import com.kungo.common.response.ApiResponse;
import com.kungo.payment.domain.entity.CommissionRule;
import com.kungo.payment.dto.request.CommissionRuleRequest;
import com.kungo.payment.dto.response.CommissionRuleResponse;
import com.kungo.payment.mapper.PaymentMapper;
import com.kungo.payment.repository.CommissionRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuration des taux de commission plateforme (globale ou par catégorie).
 * Toutes les opérations sont réservées aux administrateurs.
 */
@RestController
@RequestMapping("/admin/commission-rules")
@RequiredArgsConstructor
@Tag(name = "Commissions (admin)")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class CommissionRuleController {

    private final CommissionRuleRepository commissionRuleRepository;
    private final CategoryService categoryService;
    private final PaymentMapper paymentMapper;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Lister les règles de commission")
    public ApiResponse<List<CommissionRuleResponse>> list() {
        return ApiResponse.success(commissionRuleRepository.findAllByOrderByEffectiveFromDesc().stream()
                .map(paymentMapper::toResponse).toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une règle de commission (globale si categoryId absent)")
    public ApiResponse<CommissionRuleResponse> create(@Valid @RequestBody CommissionRuleRequest request) {
        Category category = request.getCategoryId() != null ? categoryService.getEntityById(request.getCategoryId()) : null;
        CommissionRule rule = CommissionRule.builder()
                .category(category)
                .percentage(request.getPercentage())
                .build();
        CommissionRule saved = commissionRuleRepository.save(rule);
        auditLogService.record("CommissionRule", saved.getId(), "CREATE",
                "percentage=" + request.getPercentage() + ", categoryId=" + request.getCategoryId());
        return ApiResponse.success(paymentMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Désactiver une règle de commission")
    public void deactivate(@PathVariable UUID id) {
        CommissionRule rule = commissionRuleRepository.findById(id).orElseThrow();
        rule.setActive(false);
        commissionRuleRepository.save(rule);
    }
}
