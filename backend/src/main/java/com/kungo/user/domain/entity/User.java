package com.kungo.user.domain.entity;

import com.kungo.common.audit.BaseEntity;
import com.kungo.user.domain.enums.Role;
import com.kungo.user.domain.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Compte utilisateur — racine d'agrégat pour l'authentification et l'identité.
 * Un {@code User} de rôle PROFESSIONNEL possède en 1-1 un {@code ProfessionalProfile}
 * (module professional) portant les données métier spécifiques.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "passwordHash")
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_status", columnList = "status")
})
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "locale", length = 5)
    @Builder.Default
    private String locale = "fr";

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    // ---- Comportement métier (jamais de setter public sur `status`) ----

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public void reactivate() {
        this.status = UserStatus.ACTIVE;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
