package com.kungo.professional.dto.response;

import com.kungo.professional.domain.enums.SocialPlatform;
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
public class SocialLinkResponse {
    private UUID id;
    private SocialPlatform platform;
    private String url;
}
