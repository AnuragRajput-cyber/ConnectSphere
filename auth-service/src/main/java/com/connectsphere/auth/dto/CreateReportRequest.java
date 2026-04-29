package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull ReportTargetType targetType,
        @NotBlank String targetId,
        @NotBlank @Size(max = 120) String reason,
        @Size(max = 1000) String details
) {
}
