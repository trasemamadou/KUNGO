package com.kungo.notification.mapper;

import com.kungo.notification.domain.entity.Notification;
import com.kungo.notification.dto.response.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
