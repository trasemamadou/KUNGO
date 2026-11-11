package com.kungo.user.event;

import com.kungo.user.domain.enums.Role;
import java.util.UUID;

/**
 * Publié après la création réussie d'un compte (module {@code auth}). Permet aux
 * autres modules (ex: {@code professional}) de réagir sans que {@code auth} ait à
 * les connaître directement (pattern Observer via {@code ApplicationEventPublisher}).
 */
public record UserRegisteredEvent(UUID userId, Role role) {
}
