package com.kungo.user.dto.response;

import com.kungo.user.domain.enums.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vue allégée d'un utilisateur, utilisée en imbrication dans d'autres réponses
 * (ex: auteur d'un avis, client d'une réservation) pour éviter le sur-fetch.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private UUID id;
    private String fullName;
    private String avatarUrl;
    private Role role;
}
