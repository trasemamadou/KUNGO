package com.kungo.favorite;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    @Mapping(source = "professional.id", target = "professionalId")
    @Mapping(source = "professional.user.fullName", target = "professionalName")
    @Mapping(source = "professional.user.avatarUrl", target = "professionalAvatarUrl")
    @Mapping(source = "professional.businessName", target = "businessName")
    @Mapping(source = "professional.averageRating", target = "averageRating")
    @Mapping(source = "createdAt", target = "addedAt")
    FavoriteResponse toResponse(Favorite favorite);
}
