package com.kungo.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kungo.auth.ActivationToken;
import com.kungo.auth.RegisterRequest;
import com.kungo.auth.ActivationTokenRepository;
import com.kungo.auth.PasswordResetTokenRepository;
import com.kungo.auth.RefreshTokenRepository;
import com.kungo.auth.AuthServiceImpl;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ConflictException;
import com.kungo.common.mail.EmailService;
import com.kungo.common.security.JwtProperties;
import com.kungo.common.security.JwtTokenProvider;
import com.kungo.common.security.TokenBlacklistService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.domain.enums.Role;
import com.kungo.user.domain.enums.UserStatus;
import com.kungo.user.mapper.UserMapper;
import com.kungo.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private ActivationTokenRepository activationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private JwtProperties jwtProperties;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private EmailService emailService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "activationBaseUrl", "http://localhost:4200/auth/activate");
        ReflectionTestUtils.setField(authService, "resetPasswordBaseUrl", "http://localhost:4200/auth/reset-password");
    }

    @Test
    @DisplayName("register() crée un compte PENDING et envoie l'email d'activation")
    void shouldRegisterNewClient() {
        RegisterRequest request = RegisterRequest.builder()
                .email("client@kungo.app")
                .password("Password123")
                .firstName("Awa")
                .lastName("Diop")
                .role(Role.CLIENT)
                .build();

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(activationTokenRepository).save(any(ActivationToken.class));
        verify(emailService).sendHtml(anyString(), anyString(), anyString());
        verify(eventPublisher).publishEvent(any(com.kungo.user.event.UserRegisteredEvent.class));
    }

    @Test
    @DisplayName("register() refuse un email déjà utilisé")
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@kungo.app")
                .password("Password123")
                .firstName("Awa")
                .lastName("Diop")
                .role(Role.CLIENT)
                .build();

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() refuse l'inscription directe en ADMIN")
    void shouldRejectAdminSelfRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .email("hacker@kungo.app")
                .password("Password123")
                .firstName("X")
                .lastName("Y")
                .role(Role.ADMIN)
                .build();

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    @DisplayName("activate() active le compte pour un jeton valide")
    void shouldActivateAccountWithValidToken() {
        User user = User.builder().email("client@kungo.app").status(UserStatus.PENDING).build();
        ActivationToken token = ActivationToken.builder()
                .user(user)
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(activationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        authService.activate("valid-token");

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(activationTokenRepository).delete(token);
    }

    @Test
    @DisplayName("activate() refuse un jeton expiré")
    void shouldRejectExpiredToken() {
        User user = User.builder().email("client@kungo.app").status(UserStatus.PENDING).build();
        ActivationToken token = ActivationToken.builder()
                .user(user)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(3600))
                .build();

        when(activationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.activate("expired-token")).isInstanceOf(BadRequestException.class);
        verify(userRepository, times(0)).save(any());
    }
}
