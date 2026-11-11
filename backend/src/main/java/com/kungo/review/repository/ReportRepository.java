package com.kungo.review.repository;

import com.kungo.review.domain.entity.Report;
import com.kungo.review.domain.enums.ReportStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

    long countByStatus(ReportStatus status);
}
