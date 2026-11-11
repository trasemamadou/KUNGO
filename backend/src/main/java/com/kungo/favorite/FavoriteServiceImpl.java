package com.kungo.favorite;

import com.kungo.common.exception.ConflictException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.service.ProfessionalService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProfessionalService professionalService;
    private final UserService userService;
    private final FavoriteMapper favoriteMapper;

    @Override
    public java.util.List<FavoriteResponse> listForCurrentClient() {
        return favoriteRepository.findByClientId(SecurityUtils.currentUserId()).stream()
                .map(favoriteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteResponse add(UUID professionalId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        if (favoriteRepository.existsByClientIdAndProfessionalId(currentUserId, professionalId)) {
            throw new ConflictException("error.conflict");
        }
        User client = userService.getEntityById(currentUserId);
        ProfessionalProfile professional = professionalService.getEntityById(professionalId);

        Favorite favorite = Favorite.builder().client(client).professional(professional).build();
        return favoriteMapper.toResponse(favoriteRepository.save(favorite));
    }

    @Override
    @Transactional
    public void remove(UUID professionalId) {
        favoriteRepository.findByClientIdAndProfessionalId(SecurityUtils.currentUserId(), professionalId)
                .ifPresent(favoriteRepository::delete);
    }

    @Override
    public boolean isFavorite(UUID professionalId) {
        return favoriteRepository.existsByClientIdAndProfessionalId(SecurityUtils.currentUserId(), professionalId);
    }
}
