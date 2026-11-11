package com.kungo.professional.repository;

import com.kungo.professional.domain.entity.Language;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, UUID> {

    List<Language> findByIdIn(Set<UUID> ids);
}
