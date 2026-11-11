package com.kungo.user.dto.request;

import com.kungo.user.domain.enums.Role;
import com.kungo.user.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Critères de recherche dynamique pour l'administration des utilisateurs.
 * Tous les champs sont optionnels et combinés en AND par {@code UserSpecifications}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    private String keyword;      // recherche plein texte : nom, prénom, email
    private Role role;
    private UserStatus status;
    private Boolean emailVerified;
    private String createdFrom;  // ISO-8601
    private String createdTo;    // ISO-8601
}
