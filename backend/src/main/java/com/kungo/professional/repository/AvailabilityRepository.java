package com.kungo.professional.repository;

import com.kungo.professional.domain.entity.Availability;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    List<Availability> findByProfessionalId(UUID professionalId);

    List<Availability> findByProfessionalIdAndRecurringTrue(UUID professionalId);

    List<Availability> findByProfessionalIdAndSpecificDate(UUID professionalId, LocalDate date);

    void deleteByProfessionalIdAndId(UUID professionalId, UUID id);
}
