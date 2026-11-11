package com.kungo.admin.service;

import java.util.UUID;

public interface AuditLogService {

    void record(String entityName, UUID entityId, String action, String details);
}
