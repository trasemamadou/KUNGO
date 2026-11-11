package com.kungo.user.service.impl;

import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.FileStorageService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.dto.request.UserSearchRequest;
import com.kungo.user.dto.request.UserUpdateRequest;
import com.kungo.user.dto.response.UserResponse;
import com.kungo.user.mapper.UserMapper;
import com.kungo.user.repository.UserRepository;
import com.kungo.user.service.UserService;
import com.kungo.user.specification.UserSpecifications;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String AVATAR_FOLDER = "avatars";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Override
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("user.not-found", id));
    }

    @Override
    public User getEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ResourceNotFoundException.of("user.not-found", email));
    }

    @Override
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(getEntityById(id));
    }

    @Override
    public UserResponse getCurrentUser() {
        return getById(SecurityUtils.currentUserId());
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        SecurityUtils.requireOwnerOrRole(id, "ADMIN", "SUPER_ADMIN");
        User user = getEntityById(id);
        userMapper.updateEntityFromRequest(request, user);
        User saved = userRepository.save(user);
        log.info("User {} updated", id);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(UUID id, MultipartFile file) {
        SecurityUtils.requireOwnerOrRole(id, "ADMIN", "SUPER_ADMIN");
        User user = getEntityById(id);
        String previousAvatar = user.getAvatarUrl();
        String url = fileStorageService.store(file, AVATAR_FOLDER);
        user.setAvatarUrl(url);
        User saved = userRepository.save(user);
        if (previousAvatar != null) {
            fileStorageService.delete(previousAvatar);
        }
        return userMapper.toResponse(saved);
    }

    @Override
    public Page<UserResponse> search(UserSearchRequest request, Pageable pageable) {
        return userRepository.findAll(UserSpecifications.from(request), pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void suspend(UUID id) {
        User user = getEntityById(id);
        user.suspend();
        userRepository.save(user);
        log.info("User {} suspended", id);
    }

    @Override
    @Transactional
    public void reactivate(UUID id) {
        User user = getEntityById(id);
        user.reactivate();
        userRepository.save(user);
        log.info("User {} reactivated", id);
    }

    @Override
    @Transactional
    public void ban(UUID id) {
        User user = getEntityById(id);
        user.ban();
        userRepository.save(user);
        log.info("User {} banned", id);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = getEntityById(id);
        user.markDeleted();
        userRepository.save(user);
        log.info("User {} soft-deleted", id);
    }
}
