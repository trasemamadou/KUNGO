package com.kungo.prestation.repository;

import com.kungo.prestation.domain.entity.EventService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventServiceRepository extends JpaRepository<EventService, UUID>, JpaSpecificationExecutor<EventService> {

    List<EventService> findByProfessionalId(UUID professionalId);
}
