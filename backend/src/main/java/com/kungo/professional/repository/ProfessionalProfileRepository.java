package com.kungo.professional.repository;

import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.domain.enums.VerificationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfessionalProfileRepository
        extends JpaRepository<ProfessionalProfile, UUID>, JpaSpecificationExecutor<ProfessionalProfile> {

    Optional<ProfessionalProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    long countByVerificationStatus(VerificationStatus status);
}
