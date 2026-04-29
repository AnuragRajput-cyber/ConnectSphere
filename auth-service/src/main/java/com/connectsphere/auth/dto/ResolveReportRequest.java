package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.ReportResolutionAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReportRequest(
        @NotNull ReportResolutionAction action,
        @Size(max = 1000) String resolutionNotes
) {
}
