package com.kungo.user.domain.entity;

import com.kungo.common.audit.BaseEntity;
import com.kungo.user.domain.enums.DocumentStatus;
import com.kungo.user.domain.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Document justificatif soumis par un utilisateur (pièce d'identité, registre de
 * commerce...) dans le cadre de la vérification de compte professionnel.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "user")
@Entity
@Table(name = "user_documents")
public class UserDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private DocumentType type;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    public void approve() {
        this.status = DocumentStatus.APPROVED;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        this.status = DocumentStatus.REJECTED;
        this.rejectionReason = reason;
    }
}
