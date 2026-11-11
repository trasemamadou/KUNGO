package com.kungo.professional.dto.request;

import com.kungo.professional.domain.enums.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SocialLinkRequest {

    @NotNull
    private SocialPlatform platform;

    @NotBlank
    private String url;
}
