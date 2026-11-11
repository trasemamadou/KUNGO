package com.kungo.common.security;

import com.kungo.common.exception.UnauthorizedException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Point d'accès unique à l'identité de l'utilisateur courant depuis les services.
 * Évite de dépendre directement de {@link SecurityContextHolder} partout et centralise
 * la vérification "propriétaire de la ressource" (défense en profondeur en plus de
 * {@code @PreAuthorize}).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static UUID currentUserId() {
        return getCurrentPrincipal()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new UnauthorizedException("error.unauthorized"));
    }

    public static String currentUserEmail() {
        return getCurrentPrincipal()
                .map(UserPrincipal::getUsername)
                .orElseThrow(() -> new UnauthorizedException("error.unauthorized"));
    }

    public static boolean hasRole(String role) {
        return getCurrentPrincipal()
                .map(p -> role.equalsIgnoreCase(p.getRole()))
                .orElse(false);
    }

    public static void requireOwnerOrRole(UUID resourceOwnerId, String... allowedRoles) {
        UUID currentUserId = currentUserId();
        if (currentUserId.equals(resourceOwnerId)) {
            return;
        }
        for (String role : allowedRoles) {
            if (hasRole(role)) {
                return;
            }
        }
        throw new com.kungo.common.exception.ForbiddenException("error.forbidden");
    }
}
