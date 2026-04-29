package com.connectsphere.auth.controller;

import com.connectsphere.auth.dto.CreateReportRequest;
import com.connectsphere.auth.dto.ReportResponse;
import com.connectsphere.auth.dto.ResolveReportRequest;
import com.connectsphere.auth.entity.ReportStatus;
import com.connectsphere.auth.service.ReportModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/reports", "/reports"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "User reporting and admin moderation workflow.")
public class ReportResource {

    private final ReportModerationService reportModerationService;

    public ReportResource(ReportModerationService reportModerationService) {
        this.reportModerationService = reportModerationService;
    }

    @PostMapping
    @Operation(summary = "Create a report")
    public ReportResponse createReport(@Valid @RequestBody CreateReportRequest request, Principal principal) {
        return reportModerationService.createReport(request, principal.getName());
    }

    @GetMapping("/mine")
    @Operation(summary = "List the current user's reports")
    public List<ReportResponse> getMyReports(Principal principal) {
        return reportModerationService.getMyReports(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all reports for moderation")
    public List<ReportResponse> getReports(@RequestParam(required = false) ReportStatus status) {
        return reportModerationService.getReports(status);
    }

    @PatchMapping("/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve or dismiss a report")
    public ReportResponse resolveReport(
            @PathVariable String reportId,
            @Valid @RequestBody ResolveReportRequest request,
            Principal principal
    ) {
        return reportModerationService.resolveReport(reportId, request, principal.getName());
    }
}
