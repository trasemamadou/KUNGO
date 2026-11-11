package com.kungo.notification.factory;

import com.kungo.notification.domain.entity.Notification;
import com.kungo.notification.domain.enums.NotificationChannel;
import com.kungo.notification.domain.enums.NotificationType;
import com.kungo.user.domain.entity.User;
import org.springframework.stereotype.Component;

/**
 * Construit les entités {@link Notification} de façon cohérente (pattern Factory) —
 * évite de dupliquer la logique de construction dans chaque listener d'événement.
 */
@Component
public class NotificationFactory {

    public Notification create(User user, NotificationType type, String title, String message,
                                String link, NotificationChannel channel) {
        return Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .channel(channel)
                .build();
    }
}
