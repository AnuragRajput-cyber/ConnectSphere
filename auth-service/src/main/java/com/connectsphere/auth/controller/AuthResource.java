package com.connectsphere.auth.controller;

import com.connectsphere.auth.dto.ApiMessageResponse;
import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
import com.connectsphere.auth.dto.LoginRequest;
import com.connectsphere.auth.dto.LogoutRequest;
import com.connectsphere.auth.dto.PublicUserProfileResponse;
import com.connectsphere.auth.dto.RefreshTokenRequest;
import com.connectsphere.auth.dto.RegisterPendingResponse;
import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.ResendOtpRequest;
import com.connectsphere.auth.dto.ResetPasswordRequest;
import com.connectsphere.auth.dto.TokenValidationRequest;
import com.connectsphere.auth.dto.TokenValidationResponse;
import com.connectsphere.auth.dto.UpdateProfileRequest;
import com.connectsphere.auth.dto.UserProfileResponse;
import com.connectsphere.auth.dto.UserSummaryResponse;
import com.connectsphere.auth.dto.VerifyEmailRequest;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
@Tag(name = "Auth Service", description = "Endpoints for registration, login, profile management, and token workflows.")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a user account and issues a one-time email verification code before login is allowed.")
    public ResponseEntity<RegisterPendingResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with OTP", description = "Verifies a registration OTP code and enables login for the email account.")
    public ResponseEntity<ApiMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(new ApiMessageResponse("Email verified successfully."));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend registration OTP", description = "Resends the latest OTP code for unverified accounts.")
    public ResponseEntity<RegisterPendingResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(authService.resendRegisterOtp(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset OTP", description = "Issues a one-time password reset code to the user's email (response is always generic).")
    public ResponseEntity<ApiMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(new ApiMessageResponse("If the email exists, a password reset code has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP", description = "Verifies an OTP code and sets a new password for local accounts.")
    public ResponseEntity<ApiMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiMessageResponse("Password reset successfully."));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates a user with email and password and returns JWT tokens.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout a user", description = "Revokes the current session-related token information.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiMessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiMessageResponse("Logout successful."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh an access token", description = "Uses a valid refresh token to issue a fresh access token and rotated refresh token.")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a JWT", description = "Lets downstream services verify whether a supplied access token is still valid.")
    public ResponseEntity<TokenValidationResponse> validate(@Valid @RequestBody TokenValidationRequest request) {
        return ResponseEntity.ok(authService.validateToken(request));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get the authenticated user's profile", description = "Reads the current user's profile by using the authenticated principal.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> getProfile(Principal principal) {
        User user = authService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update the authenticated user's profile", description = "Updates editable profile fields such as full name, bio, and profile picture URL.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateProfile(principal.getName(), request));
    }

    @PatchMapping("/password")
    @Operation(summary = "Change the authenticated user's password", description = "Checks the current password and replaces it with a new bcrypt-hashed password.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiMessageResponse> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(new ApiMessageResponse("Password changed successfully."));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Searches active users by username or full name.")
    public ResponseEntity<List<UserSummaryResponse>> searchUsers(@RequestParam("query") String query) {
        return ResponseEntity.ok(authService.searchUsers(query));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a public user profile", description = "Returns the public-facing profile data used by guest and social views.")
    public ResponseEntity<PublicUserProfileResponse> getPublicUserProfile(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getPublicUserProfile(userId));
    }

    @PatchMapping("/deactivate")
    @Operation(summary = "Deactivate the authenticated user's account", description = "Marks the signed-in user as inactive instead of hard deleting the account.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiMessageResponse> deactivateAccount(Principal principal) {
        authService.deactivateAccount(principal.getName());
        return ResponseEntity.ok(new ApiMessageResponse("Account deactivated successfully."));
    }

    @GetMapping("/users/by-email/{email}")
    @Operation(summary = "Lookup a user by email", description = "Returns a user profile for inter-service communication or administrative lookups.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(UserProfileResponse.from(authService.getUserByEmail(email)));
    }
}
