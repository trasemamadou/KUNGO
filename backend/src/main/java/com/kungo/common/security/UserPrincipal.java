package com.kungo.common.security;

import java.util.Collection;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Implémentation générique de {@link UserDetails} découplée de l'entité JPA {@code User}
 * (qui vit dans le module {@code user}), afin que le module {@code common.security} ne
 * dépende d'aucun module métier. Construite par {@code CustomUserDetailsService} (module user).
 */
@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String fullName;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
