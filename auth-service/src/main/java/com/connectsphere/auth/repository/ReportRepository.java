package com.connectsphere.auth.repository;

import com.connectsphere.auth.entity.Report;
import com.connectsphere.auth.entity.ReportStatus;
import com.connectsphere.auth.entity.ReportTargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByReporterIdOrderByCreatedAtDesc(String reporterId);

    Optional<Report> findFirstByReporterIdAndTargetIdAndTargetTypeAndStatus(
            String reporterId,
            String targetId,
            ReportTargetType targetType,
            ReportStatus status
    );
}
