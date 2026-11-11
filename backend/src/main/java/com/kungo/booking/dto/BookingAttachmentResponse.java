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
public class BookingAttachmentResponse {
    private UUID id;
    private String fileName;
    private String url;
    private UUID uploadedById;
    private Instant createdAt;
}
