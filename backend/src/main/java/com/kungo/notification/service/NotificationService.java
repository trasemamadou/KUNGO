package com.kungo.notification.service;

import com.kungo.notification.domain.enums.NotificationType;
import com.kungo.notification.dto.response.NotificationResponse;
import com.kungo.user.domain.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Persiste la notification, l'émet en temps réel (WebSocket) et, si pertinent,
     * déclenche l'email correspondant. Utilisé par les listeners d'événements
     * (booking, payment, professional...).
     */
    void notify(User user, NotificationType type, String title, String message, String link);

    Page<NotificationResponse> listForCurrentUser(Pageable pageable);

    long unreadCount();

    void markRead(UUID id);

    void markAllRead();
}
