package com.kungo.payment.repository;

import com.kungo.payment.domain.entity.CommissionRule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, UUID> {

    Optional<CommissionRule> findFirstByCategoryIdAndActiveTrueOrderByEffectiveFromDesc(UUID categoryId);

    Optional<CommissionRule> findFirstByCategoryIsNullAndActiveTrueOrderByEffectiveFromDesc();

    List<CommissionRule> findAllByOrderByEffectiveFromDesc();
}
