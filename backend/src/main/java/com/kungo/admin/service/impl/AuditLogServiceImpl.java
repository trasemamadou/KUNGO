package com.kungo.admin.service.impl;

import com.kungo.admin.domain.entity.AuditLog;
import com.kungo.admin.repository.AuditLogRepository;
import com.kungo.admin.service.AuditLogService;
import com.kungo.common.security.SecurityUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void record(String entityName, UUID entityId, String action, String details) {
        AuditLog log = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .performedBy(SecurityUtils.getCurrentPrincipal().map(p -> p.getId()).orElse(null))
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}
