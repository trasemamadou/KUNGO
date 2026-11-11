package com.kungo.favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByClientId(UUID clientId);

    Optional<Favorite> findByClientIdAndProfessionalId(UUID clientId, UUID professionalId);

    boolean existsByClientIdAndProfessionalId(UUID clientId, UUID professionalId);
}
