package com.kungo.auth;

import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ConflictException;
import com.kungo.common.exception.UnauthorizedException;
import com.kungo.common.mail.EmailService;
import com.kungo.common.security.JwtProperties;
import com.kungo.common.security.JwtTokenProvider;
import com.kungo.common.security.UserPrincipal;
import com.kungo.user.domain.entity.User;
import com.kungo.user.domain.enums.Role;
import com.kungo.user.event.UserRegisteredEvent;
import com.kungo.user.mapper.UserMapper;
import com.kungo.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final long ACTIVATION_TOKEN_TTL_HOURS = 24;
    private static final long RESET_TOKEN_TTL_HOURS = 1;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final com.kungo.common.security.TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.mail.frontend-activation-url}")
    private String activationBaseUrl;

    @Value("${app.mail.frontend-reset-password-url}")
    private String resetPasswordBaseUrl;

    @Override
    public void register(RegisterRequest request) {
        if (request.getRole() != Role.CLIENT && request.getRole() != Role.PROFESSIONNEL) {
            throw new BadRequestException("error.validation");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("user.email-already-exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();
        userRepository.save(user);

        issueActivationTokenAndSendEmail(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getRole()));
        log.info("New user registered: {} ({})", user.getEmail(), user.getRole());
    }

    @Override
    public void activate(String token) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("auth.token-invalid"));

        if (activationToken.isExpired()) {
            throw new BadRequestException("auth.token-expired");
        }

        User user = activationToken.getUser();
        user.activate();
        userRepository.save(user);
        activationTokenRepository.delete(activationToken);
        log.info("User {} activated", user.getEmail());
    }

    @Override
    public void resendActivation(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("user.not-found"));
        if (user.isActive()) {
            return;
        }
        issueActivationTokenAndSendEmail(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("error.unauthorized"));

        user.recordLogin();
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = issueRefreshToken(user);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("auth.token-invalid"));

        if (!existing.isValid()) {
            throw new UnauthorizedException("auth.token-expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        RefreshToken newRefreshToken = issueRefreshToken(user);

        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(principal);

        return buildAuthResponse(accessToken, newRefreshToken.getToken(), user);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            tokenBlacklistService.blacklist(accessToken);
        }
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusSeconds(RESET_TOKEN_TTL_HOURS * 3600))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String link = resetPasswordBaseUrl + "?token=" + resetToken.getToken();
            emailService.sendHtml(user.getEmail(), "Réinitialisation de votre mot de passe KUNGO",
                    AuthMailTemplates.passwordReset(user.getFirstName(), link));
        });
        // Ne jamais révéler si l'email existe ou non (anti énumération de comptes).
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("auth.token-invalid"));

        if (!resetToken.isValid()) {
            throw new BadRequestException("auth.token-expired");
        }

        User user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("Password reset for user {}", user.getEmail());
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("error.unauthorized"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("user.invalid-credentials");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    private void issueActivationTokenAndSendEmail(User user) {
        ActivationToken activationToken = ActivationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(ACTIVATION_TOKEN_TTL_HOURS * 3600))
                .build();
        activationTokenRepository.save(activationToken);

        String link = activationBaseUrl + "?token=" + activationToken.getToken();
        emailService.sendHtml(user.getEmail(), "Activez votre compte KUNGO",
                AuthMailTemplates.activation(user.getFirstName(), link));
    }

    private RefreshToken issueRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }
}
