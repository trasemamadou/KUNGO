package com.kungo.category;

import java.util.List;
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
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private String image;
    private UUID parentId;
    private String parentName;
    private boolean active;
    private int displayOrder;
    private List<CategorySummaryResponse> children;
}
