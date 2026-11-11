package com.kungo.auth;

import java.util.UUID;

public interface AuthService {

    void register(RegisterRequest request);

    void activate(String token);

    void resendActivation(String email);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken, String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);
}
