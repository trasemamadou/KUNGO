package com.kungo.user.specification;

import com.kungo.common.specification.GenericSpecificationBuilder;
import com.kungo.common.specification.SearchOperator;
import com.kungo.user.domain.entity.User;
import com.kungo.user.dto.request.UserSearchRequest;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Traduit un {@link UserSearchRequest} en {@link Specification} dynamique via le
 * moteur de filtre générique commun ({@code GenericSpecificationBuilder}).
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> from(UserSearchRequest request) {
        GenericSpecificationBuilder<User> builder = new GenericSpecificationBuilder<>();

        if (request.getRole() != null) {
            builder.with("role", SearchOperator.EQUALS, request.getRole());
        }
        if (request.getStatus() != null) {
            builder.with("status", SearchOperator.EQUALS, request.getStatus());
        }
        if (request.getEmailVerified() != null) {
            builder.with("emailVerified", SearchOperator.EQUALS, request.getEmailVerified());
        }
        if (StringUtils.hasText(request.getCreatedFrom()) && StringUtils.hasText(request.getCreatedTo())) {
            builder.with("createdAt", SearchOperator.BETWEEN,
                    Instant.parse(request.getCreatedFrom()), Instant.parse(request.getCreatedTo()));
        }
        if (StringUtils.hasText(request.getKeyword())) {
            builder.withFullText(request.getKeyword(), "firstName", "lastName", "email");
        }

        return builder.build();
    }
}
