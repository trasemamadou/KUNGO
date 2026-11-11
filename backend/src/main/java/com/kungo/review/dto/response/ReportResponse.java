package com.kungo.review.dto.response;

import com.kungo.review.domain.enums.ReportStatus;
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
public class ReportResponse {
    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private UUID reportedUserId;
    private UUID reportedReviewId;
    private String reason;
    private ReportStatus status;
    private Instant createdAt;
}
