package com.connectsphere.auth.controller;

import com.connectsphere.auth.dto.AdminStatsResponse;
import com.connectsphere.auth.dto.AdminPlatformOverviewResponse;
import com.connectsphere.auth.dto.AdminSystemOverviewResponse;
import com.connectsphere.auth.dto.AdminUserResponse;
import com.connectsphere.auth.dto.ApiMessageResponse;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.exception.NotFoundException;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.service.AdminInsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/admin", "/admin"})
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin-only user management and analytics endpoints.")
public class AdminResource {

    private final UserRepository userRepository;
    private final AdminInsightsService adminInsightsService;

    public AdminResource(UserRepository userRepository, AdminInsightsService adminInsightsService) {
        this.userRepository = userRepository;
        this.adminInsightsService = adminInsightsService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(AdminUserResponse::from).toList());
    }

    @PatchMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiMessageResponse> suspendUser(@PathVariable String userId, Principal principal) {
        User actor = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new NotFoundException("Actor not found."));
        if (actor.getUserId().equals(userId.trim())) {
            throw new BadRequestException("Admins cannot suspend themselves.");
        }

        User user = userRepository.findByUserId(userId.trim()).orElseThrow(() -> new NotFoundException("User not found."));
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiMessageResponse("User suspended."));
    }

    @PatchMapping("/users/{userId}/reactivate")
    @Operation(summary = "Reactivate a user account")
    public ResponseEntity<ApiMessageResponse> reactivateUser(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId.trim()).orElseThrow(() -> new NotFoundException("User not found."));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiMessageResponse("User reactivated."));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Permanently delete a user account")
    public ResponseEntity<ApiMessageResponse> deleteUser(@PathVariable String userId, Principal principal) {
        User actor = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new NotFoundException("Actor not found."));
        if (actor.getUserId().equals(userId.trim())) {
            throw new BadRequestException("Admins cannot delete themselves.");
        }
        if (!userRepository.existsById(userId.trim())) {
            throw new NotFoundException("User not found.");
        }
        userRepository.deleteById(userId.trim());
        return ResponseEntity.ok(new ApiMessageResponse("User deleted."));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get platform user stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminInsightsService.buildUserStats());
    }

    @GetMapping("/platform-overview")
    @Operation(summary = "Get aggregated admin overview")
    public ResponseEntity<AdminPlatformOverviewResponse> getPlatformOverview() {
        return ResponseEntity.ok(adminInsightsService.buildPlatformOverview());
    }

    @GetMapping("/system-overview")
    @Operation(summary = "Get service health overview")
    public ResponseEntity<AdminSystemOverviewResponse> getSystemOverview() {
        return ResponseEntity.ok(adminInsightsService.buildSystemOverview());
    }
}
