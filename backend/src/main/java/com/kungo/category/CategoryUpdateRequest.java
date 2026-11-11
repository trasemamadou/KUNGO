package com.kungo.category;

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
public class CategoryUpdateRequest {

    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    private String icon;
    private String image;
    private UUID parentId;
    private Integer displayOrder;
    private Boolean active;
}
