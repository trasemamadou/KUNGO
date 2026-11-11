package com.kungo.prestation.mapper;

import com.kungo.category.CategoryMapper;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.prestation.domain.entity.ServiceMedia;
import com.kungo.prestation.dto.response.ServiceMediaResponse;
import com.kungo.prestation.dto.response.ServiceResponse;
import com.kungo.prestation.dto.response.ServiceSummaryResponse;
import com.kungo.professional.mapper.ProfessionalMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ProfessionalMapper.class})
public interface EventServiceMapper {

    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "price.currency", target = "currency")
    @Mapping(target = "media", ignore = true)
    ServiceResponse toResponse(EventService service);

    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "price.currency", target = "currency")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "professional.id", target = "professionalId")
    @Mapping(source = "professional.user.fullName", target = "professionalName")
    @Mapping(source = "professional.user.avatarUrl", target = "professionalAvatarUrl")
    @Mapping(source = "professional.address.city", target = "city")
    @Mapping(source = "professional.averageRating", target = "professionalRating")
    @Mapping(source = "professional.verified", target = "professionalVerified")
    @Mapping(target = "coverImageUrl", ignore = true)
    ServiceSummaryResponse toSummary(EventService service);

    ServiceMediaResponse toResponse(ServiceMedia media);
}
