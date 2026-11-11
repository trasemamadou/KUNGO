package com.kungo.user.mapper;

import com.kungo.user.domain.entity.User;
import com.kungo.user.dto.request.UserUpdateRequest;
import com.kungo.user.dto.response.UserResponse;
import com.kungo.user.dto.response.UserSummaryResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Tous les mappings Entity <-> DTO sont réalisés par MapStruct (aucun mapping manuel).
 * {@code componentModel = "spring"} génère un bean injectable directement dans les services.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    UserSummaryResponse toSummary(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);
}
