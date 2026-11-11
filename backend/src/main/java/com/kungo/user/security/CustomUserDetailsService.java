package com.kungo.user.security;

import com.kungo.common.security.UserPrincipal;
import com.kungo.user.domain.entity.User;
import com.kungo.user.domain.enums.UserStatus;
import com.kungo.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seul point de couplage entre Spring Security (module {@code common.security}) et
 * le domaine {@code User}. Construit un {@link UserPrincipal} générique à partir de
 * l'entité JPA, ajoutant le préfixe {@code ROLE_} exigé par {@code hasRole(...)}.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .enabled(user.getStatus() != UserStatus.PENDING)
                .accountNonLocked(user.getStatus() != UserStatus.SUSPENDED && user.getStatus() != UserStatus.BANNED)
                .build();
    }
}
