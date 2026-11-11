package com.kungo.booking.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingMessageResponse {
    private UUID id;
    private UUID senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private String attachmentUrl;
    private String attachmentName;
    private boolean read;
    private Instant createdAt;
}
