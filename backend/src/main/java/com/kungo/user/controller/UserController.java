package com.kungo.user.controller;

import com.kungo.admin.service.AuditLogService;
import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import com.kungo.common.util.MessageUtils;
import com.kungo.user.dto.request.UserSearchRequest;
import com.kungo.user.dto.request.UserUpdateRequest;
import com.kungo.user.dto.response.UserResponse;
import com.kungo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final MessageUtils messageUtils;
    private final AuditLogService auditLogService;

    @GetMapping("/me")
    @Operation(summary = "Profil de l'utilisateur authentifié")
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un utilisateur par id")
    public ApiResponse<UserResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getById(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour le profil (propriétaire ou admin)")
    public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(messageUtils.get("user.updated"), userService.update(id, request));
    }

    @PostMapping("/{id}/avatar")
    @Operation(summary = "Téléverser l'avatar")
    public ApiResponse<UserResponse> uploadAvatar(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.uploadAvatar(id, file));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATEUR')")
    @Operation(summary = "Recherche dynamique multicritère des utilisateurs (admin)")
    public ApiResponse<PageResponse<UserResponse>> search(UserSearchRequest request, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(userService.search(request, pageable)));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Suspendre un compte")
    public ApiResponse<Void> suspend(@PathVariable UUID id) {
        userService.suspend(id);
        auditLogService.record("User", id, "SUSPEND", null);
        return ApiResponse.message(messageUtils.get("user.suspended"));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Réactiver un compte")
    public ApiResponse<Void> reactivate(@PathVariable UUID id) {
        userService.reactivate(id);
        auditLogService.record("User", id, "REACTIVATE", null);
        return ApiResponse.message(messageUtils.get("user.reactivated"));
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Bannir un compte")
    public ApiResponse<Void> ban(@PathVariable UUID id) {
        userService.ban(id);
        auditLogService.record("User", id, "BAN", null);
        return ApiResponse.message(messageUtils.get("user.banned"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer (soft-delete) un compte")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
