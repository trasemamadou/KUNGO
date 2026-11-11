package com.kungo.category;

import com.kungo.common.specification.GenericSpecificationBuilder;
import com.kungo.common.specification.SearchOperator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CategorySpecifications {

    private CategorySpecifications() {
    }

    public static Specification<Category> from(CategorySearchRequest request) {
        GenericSpecificationBuilder<Category> builder = new GenericSpecificationBuilder<>();

        if (request.getActive() != null) {
            builder.with("active", SearchOperator.EQUALS, request.getActive());
        }
        if (Boolean.TRUE.equals(request.getRootOnly())) {
            builder.with("parent", SearchOperator.IS_NULL, null);
        } else if (request.getParentId() != null) {
            builder.with("parent.id", SearchOperator.EQUALS, request.getParentId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            builder.withFullText(request.getKeyword(), "name", "description");
        }

        return builder.build();
    }
}
