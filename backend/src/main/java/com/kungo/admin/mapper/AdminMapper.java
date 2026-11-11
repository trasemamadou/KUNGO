package com.kungo.admin.mapper;

import com.kungo.admin.domain.entity.AuditLog;
import com.kungo.admin.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
