package com.kungo.auth;

import com.kungo.common.response.ApiResponse;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;
    private final MessageUtils messageUtils;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscription (client ou professionnel)")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.message(messageUtils.get("auth.registered"));
    }

    @GetMapping("/activate")
    @Operation(summary = "Activation du compte via le lien reçu par email")
    public ApiResponse<Void> activate(@RequestParam String token) {
        authService.activate(token);
        return ApiResponse.message(messageUtils.get("auth.activated"));
    }

    @PostMapping("/resend-activation")
    @Operation(summary = "Renvoyer l'email d'activation")
    public ApiResponse<Void> resendActivation(@RequestParam String email) {
        authService.resendActivation(email);
        return ApiResponse.message(messageUtils.get("auth.activation-resent"));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir l'access token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion (révoque le refresh token, blackliste l'access token)")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                     @Valid @RequestBody RefreshTokenRequest request) {
        String accessToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        authService.logout(accessToken, request.getRefreshToken());
        return ApiResponse.message(messageUtils.get("auth.logged-out"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de réinitialisation de mot de passe")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.message(messageUtils.get("auth.reset-email-sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe via le token reçu par email")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.message(messageUtils.get("auth.password-reset"));
    }

    @PatchMapping("/change-password")
    @Operation(summary = "Changer le mot de passe (authentifié)")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.currentUserId(), request);
        return ApiResponse.message(messageUtils.get("auth.password-changed"));
    }
}
