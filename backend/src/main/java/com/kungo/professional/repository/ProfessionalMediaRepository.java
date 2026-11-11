package com.kungo.professional.repository;

import com.kungo.professional.domain.entity.ProfessionalMedia;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalMediaRepository extends JpaRepository<ProfessionalMedia, UUID> {

    List<ProfessionalMedia> findByProfessionalIdOrderByDisplayOrderAsc(UUID professionalId);

    void deleteByProfessionalIdAndId(UUID professionalId, UUID id);
}
