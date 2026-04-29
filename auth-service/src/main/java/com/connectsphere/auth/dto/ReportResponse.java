package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.Report;
import com.connectsphere.auth.entity.ReportResolutionAction;
import com.connectsphere.auth.entity.ReportStatus;
import com.connectsphere.auth.entity.ReportTargetType;
import java.time.Instant;

public record ReportResponse(
        String reportId,
        String reporterId,
        ReportTargetType targetType,
        String targetId,
        String reason,
        String details,
        ReportStatus status,
        ReportResolutionAction resolutionAction,
        String resolutionNotes,
        String resolvedBy,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getReportId(),
                report.getReporterId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getResolutionAction(),
                report.getResolutionNotes(),
                report.getResolvedBy(),
                report.getResolvedAt(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
