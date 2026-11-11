package com.kungo.professional.repository;

import com.kungo.professional.domain.entity.SocialLink;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLinkRepository extends JpaRepository<SocialLink, UUID> {

    List<SocialLink> findByProfessionalId(UUID professionalId);

    void deleteByProfessionalIdAndId(UUID professionalId, UUID id);
}
