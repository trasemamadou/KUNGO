package com.kungo.notification.service.impl;

import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.mail.EmailService;
import com.kungo.common.security.SecurityUtils;
import com.kungo.notification.domain.entity.Notification;
import com.kungo.notification.domain.enums.NotificationChannel;
import com.kungo.notification.domain.enums.NotificationType;
import com.kungo.notification.dto.response.NotificationResponse;
import com.kungo.notification.factory.NotificationFactory;
import com.kungo.notification.mapper.NotificationMapper;
import com.kungo.notification.repository.NotificationRepository;
import com.kungo.notification.service.NotificationService;
import com.kungo.user.domain.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final String WS_DESTINATION = "/queue/notifications";

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @Override
    @Transactional
    public void notify(User user, NotificationType type, String title, String message, String link) {
        Notification notification = notificationFactory.create(user, type, title, message, link, NotificationChannel.WEBSOCKET);
        Notification saved = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(user.getEmail(), WS_DESTINATION, notificationMapper.toResponse(saved));

        emailService.sendHtml(user.getEmail(), title,
                "<p>%s</p>".formatted(message));

        log.debug("Notification '{}' sent to user {}", type, user.getId());
    }

    @Override
    public Page<NotificationResponse> listForCurrentUser(Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.currentUserId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public long unreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(SecurityUtils.currentUserId());
    }

    @Override
    @Transactional
    public void markRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("error.not-found", id));
        SecurityUtils.requireOwnerOrRole(notification.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        notification.markRead();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead() {
        notificationRepository.markAllRead(SecurityUtils.currentUserId());
    }
}
