package com.kungo.prestation.repository;

import com.kungo.prestation.domain.entity.ServiceMedia;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceMediaRepository extends JpaRepository<ServiceMedia, UUID> {

    List<ServiceMedia> findByServiceIdOrderByDisplayOrderAsc(UUID serviceId);

    void deleteByServiceIdAndId(UUID serviceId, UUID id);
}
