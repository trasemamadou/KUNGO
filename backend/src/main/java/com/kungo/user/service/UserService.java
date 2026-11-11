package com.kungo.user.service;

import com.kungo.user.domain.entity.User;
import com.kungo.user.dto.request.UserSearchRequest;
import com.kungo.user.dto.request.UserUpdateRequest;
import com.kungo.user.dto.response.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getEntityById(UUID id);

    User getEntityByEmail(String email);

    UserResponse getById(UUID id);

    UserResponse getCurrentUser();

    UserResponse update(UUID id, UserUpdateRequest request);

    UserResponse uploadAvatar(UUID id, MultipartFile file);

    Page<UserResponse> search(UserSearchRequest request, Pageable pageable);

    void suspend(UUID id);

    void reactivate(UUID id);

    void ban(UUID id);

    void delete(UUID id);
}
