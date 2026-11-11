package com.kungo.professional.repository;

import com.kungo.professional.domain.entity.Skill;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findByIdIn(Set<UUID> ids);
}
