package com.kungo.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ReportCreateRequest {

    private UUID reportedUserId;
    private UUID reportedReviewId;

    @NotBlank
    @Size(max = 1000)
    private String reason;
}
