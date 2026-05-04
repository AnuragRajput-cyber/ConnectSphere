package com.connectsphere.auth.service;

import com.connectsphere.auth.dto.CreateReportRequest;
import com.connectsphere.auth.dto.ReportResponse;
import com.connectsphere.auth.dto.ResolveReportRequest;
import com.connectsphere.auth.entity.Report;
import com.connectsphere.auth.entity.ReportResolutionAction;
import com.connectsphere.auth.entity.ReportStatus;
import com.connectsphere.auth.entity.ReportTargetType;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.exception.NotFoundException;
import com.connectsphere.auth.repository.ReportRepository;
import com.connectsphere.auth.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class ReportModerationService {

    private static final String REPORTED_USER_NOT_FOUND = "Reported user not found.";

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final String postServiceBaseUrl;
    private final String commentServiceBaseUrl;
    private final String notificationServiceBaseUrl;

    public ReportModerationService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            @Value("${app.services.post-base-url:http://localhost:8082}") String postServiceBaseUrl,
            @Value("${app.services.comment-base-url:http://localhost:8083}") String commentServiceBaseUrl,
            @Value("${app.services.notification-base-url:http://localhost:8086}") String notificationServiceBaseUrl
    ) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.restClient = RestClient.builder().build();
        this.postServiceBaseUrl = postServiceBaseUrl;
        this.commentServiceBaseUrl = commentServiceBaseUrl;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    public ReportResponse createReport(CreateReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Reporter not found."));

        validateTarget(request.targetType(), request.targetId());

        reportRepository.findFirstByReporterIdAndTargetIdAndTargetTypeAndStatus(
                        reporter.getUserId(),
                        request.targetId().trim(),
                        request.targetType(),
                        ReportStatus.OPEN
                )
                .ifPresent(existing -> {
                    throw new BadRequestException("An open report already exists for this target.");
                });

        Report report = new Report();
        report.setReporterId(reporter.getUserId());
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId().trim());
        report.setReason(request.reason().trim());
        report.setDetails(blankToNull(request.details()));
        report.setStatus(ReportStatus.OPEN);

        return ReportResponse.from(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getMyReports(String reporterEmail) {
        User reporter = userRepository.findByEmail(reporterEmail.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Reporter not found."));

        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporter.getUserId()).stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(ReportStatus status) {
        List<Report> reports = status == null
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusOrderByCreatedAtDesc(status);
        return reports.stream().map(ReportResponse::from).toList();
    }

    public ReportResponse resolveReport(String reportId, ResolveReportRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Admin not found."));
        Report report = reportRepository.findById(reportId.trim())
                .orElseThrow(() -> new NotFoundException("Report not found."));

        if (report.getStatus() != ReportStatus.OPEN) {
            throw new BadRequestException("Only open reports can be resolved.");
        }

        applyResolution(report, request.action(), admin);
        report.setResolutionAction(request.action());
        report.setResolutionNotes(blankToNull(request.resolutionNotes()));
        report.setResolvedBy(admin.getUserId());
        report.setResolvedAt(Instant.now());
        report.setStatus(request.action() == ReportResolutionAction.DISMISS ? ReportStatus.DISMISSED : ReportStatus.RESOLVED);

        Report saved = reportRepository.save(report);
        notifyReporter(saved, admin);
        return ReportResponse.from(saved);
    }

    private void applyResolution(Report report, ReportResolutionAction action, User admin) {
        switch (action) {
            case DISMISS -> {
                // Dismissal only changes the report state; there is no downstream target mutation.
            }
            case REMOVE_POST -> {
                requireTargetType(report, ReportTargetType.POST);
                restClient.delete()
                        .uri(postServiceBaseUrl + "/api/v1/posts/{postId}", report.getTargetId())
                        .header("X-User-Id", admin.getUserId())
                        .header("X-User-Role", admin.getRole().name())
                        .retrieve()
                        .toBodilessEntity();
            }
            case REMOVE_COMMENT -> {
                requireTargetType(report, ReportTargetType.COMMENT);
                restClient.delete()
                        .uri(commentServiceBaseUrl + "/api/v1/comments/{commentId}", report.getTargetId())
                        .header("X-User-Id", admin.getUserId())
                        .header("X-User-Role", admin.getRole().name())
                        .retrieve()
                        .toBodilessEntity();
            }
            case SUSPEND_USER -> {
                requireTargetType(report, ReportTargetType.USER);
                User user = userRepository.findByUserId(report.getTargetId())
                        .orElseThrow(() -> new NotFoundException(REPORTED_USER_NOT_FOUND));
                user.setActive(false);
                userRepository.save(user);
            }
            case DELETE_USER -> {
                requireTargetType(report, ReportTargetType.USER);
                User user = userRepository.findByUserId(report.getTargetId())
                        .orElseThrow(() -> new NotFoundException(REPORTED_USER_NOT_FOUND));
                if (user.getUserId().equals(admin.getUserId())) {
                    throw new BadRequestException("Admins cannot delete themselves through moderation.");
                }
                userRepository.delete(user);
            }
        }
    }

    private void notifyReporter(Report report, User admin) {
        try {
            restClient.post()
                    .uri(notificationServiceBaseUrl + "/api/v1/notifications")
                    .body(Map.of(
                            "recipientId", report.getReporterId(),
                            "actorId", admin.getUserId(),
                            "type", "REPORT",
                            "message", report.getStatus() == ReportStatus.DISMISSED
                                    ? "dismissed your report"
                                    : "resolved your report",
                            "targetId", report.getTargetId(),
                            "targetType", report.getTargetType().name()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Report resolution should succeed even if notifications are temporarily unavailable.
        }
    }

    private void validateTarget(ReportTargetType targetType, String targetId) {
        String normalizedTargetId = targetId == null ? "" : targetId.trim();
        if (normalizedTargetId.isBlank()) {
            throw new BadRequestException("Target id is required.");
        }

        switch (targetType) {
            case POST -> restClient.get()
                    .uri(postServiceBaseUrl + "/api/v1/posts/{postId}", normalizedTargetId)
                    .retrieve()
                    .toBodilessEntity();
            case COMMENT -> restClient.get()
                    .uri(commentServiceBaseUrl + "/api/v1/comments/{commentId}", normalizedTargetId)
                    .retrieve()
                    .toBodilessEntity();
            case USER -> userRepository.findByUserId(normalizedTargetId)
                    .orElseThrow(() -> new NotFoundException(REPORTED_USER_NOT_FOUND));
        }
    }

    private void requireTargetType(Report report, ReportTargetType expectedType) {
        if (report.getTargetType() != expectedType) {
            throw new BadRequestException("The selected moderation action does not match the report target type.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
