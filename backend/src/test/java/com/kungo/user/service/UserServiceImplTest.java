package com.kungo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kungo.common.util.FileStorageService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.domain.enums.UserStatus;
import com.kungo.user.mapper.UserMapper;
import com.kungo.user.repository.UserRepository;
import com.kungo.user.service.impl.UserServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("suspend() passe le statut à SUSPENDED")
    void shouldSuspendUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().status(UserStatus.ACTIVE).build();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.suspend(userId);

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("ban() passe le statut à BANNED")
    void shouldBanUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().status(UserStatus.ACTIVE).build();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.ban(userId);

        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    @DisplayName("reactivate() ramène le compte à ACTIVE")
    void shouldReactivateUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().status(UserStatus.SUSPENDED).build();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.reactivate(userId);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("delete() effectue un soft-delete")
    void shouldSoftDeleteUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.delete(userId);

        assertThat(user.isDeleted()).isTrue();
    }
}
