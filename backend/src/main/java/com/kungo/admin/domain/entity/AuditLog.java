package com.kungo.admin.domain.entity;

import com.kungo.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Trace des actions sensibles de la plateforme (suspension de compte, validation
 * professionnelle, changement de commission...) à des fins d'audit et de conformité.
 * Écrite explicitement par les services concernés via {@code AuditLogService}
 * (plutôt qu'interceptée génériquement par AOP, pour garder le contenu du message
 * lisible et intentionnel).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_entity", columnList = "entity_name, entity_id"),
        @Index(name = "idx_audit_logs_performed_by", columnList = "performed_by")
})
public class AuditLog extends BaseEntity {

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "details", length = 2000)
    private String details;
}
