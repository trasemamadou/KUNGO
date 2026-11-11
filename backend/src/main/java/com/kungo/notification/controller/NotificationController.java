package com.kungo.notification.controller;

import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import com.kungo.notification.dto.response.NotificationResponse;
import com.kungo.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lister mes notifications")
    public ApiResponse<PageResponse<NotificationResponse>> list(Pageable pageable) {
        return ApiResponse.success(PageResponse.of(notificationService.listForCurrentUser(pageable)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Nombre de notifications non lues")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.success(notificationService.unreadCount());
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ApiResponse<Void> markRead(@PathVariable UUID id) {
        notificationService.markRead(id);
        return ApiResponse.message("OK");
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.message("OK");
    }
}
