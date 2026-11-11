package com.kungo.booking.specification;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.dto.BookingSearchRequest;
import com.kungo.common.specification.GenericSpecificationBuilder;
import com.kungo.common.specification.SearchOperator;
import org.springframework.data.jpa.domain.Specification;

public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> from(BookingSearchRequest request) {
        GenericSpecificationBuilder<Booking> builder = new GenericSpecificationBuilder<>();

        if (request.getClientId() != null) {
            builder.with("client.id", SearchOperator.EQUALS, request.getClientId());
        }
        if (request.getProfessionalId() != null) {
            builder.with("professional.id", SearchOperator.EQUALS, request.getProfessionalId());
        }
        if (request.getStatus() != null) {
            builder.with("status", SearchOperator.EQUALS, request.getStatus());
        }
        if (request.getEventDateFrom() != null && request.getEventDateTo() != null) {
            builder.with("eventDate", SearchOperator.BETWEEN, request.getEventDateFrom(), request.getEventDateTo());
        }

        return builder.build();
    }
}
