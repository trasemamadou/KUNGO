package com.kungo.prestation.dto.response;

import com.kungo.common.domain.MediaType;
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
public class ServiceMediaResponse {
    private UUID id;
    private MediaType type;
    private String url;
    private int displayOrder;
}
