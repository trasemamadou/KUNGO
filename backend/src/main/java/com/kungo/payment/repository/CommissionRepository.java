package com.kungo.payment.repository;

import com.kungo.payment.domain.entity.Commission;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommissionRepository extends JpaRepository<Commission, UUID> {

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Commission c")
    BigDecimal sumAllCommissions();
}
