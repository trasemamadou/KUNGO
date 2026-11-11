package com.kungo.review.dto.response;

import com.kungo.review.domain.enums.ModerationStatus;
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
public class ReviewResponse {
    private UUID id;
    private UUID bookingId;
    private UUID authorId;
    private String authorName;
    private String authorAvatarUrl;
    private UUID targetId;
    private int rating;
    private int punctuality;
    private int quality;
    private int communication;
    private int valueForMoney;
    private int professionalism;
    private String comment;
    private String professionalResponse;
    private ModerationStatus moderationStatus;
    private Instant createdAt;
}
