package com.kungo.user.dto.response;

import com.kungo.user.domain.enums.Role;
import com.kungo.user.domain.enums.UserStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private UserStatus status;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    private String avatarUrl;
    private String locale;
    private Instant lastLoginAt;
    private Instant createdAt;
}
