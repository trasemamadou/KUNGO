package com.kungo.category;

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
public class CategorySearchRequest {
    private String keyword;
    private UUID parentId;
    private Boolean active;
    private Boolean rootOnly;
}
