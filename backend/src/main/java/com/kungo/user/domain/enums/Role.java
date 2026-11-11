package com.kungo.user.domain.enums;

/**
 * Rôles applicatifs. Le préfixe {@code ROLE_} exigé par Spring Security est ajouté
 * dynamiquement lors de la construction des {@code GrantedAuthority} (voir
 * {@code CustomUserDetailsService}), jamais stocké en base.
 */
public enum Role {
    SUPER_ADMIN,
    ADMIN,
    MODERATEUR,
    CLIENT,
    PROFESSIONNEL
}
