package com.connectsphere.auth.service;

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
import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.OtpPurpose;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.exception.NotFoundException;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.security.JwtTokenService;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final EmailOtpService emailOtpService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            EmailOtpService emailOtpService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.emailOtpService = emailOtpService;
    }

    @Override
    public RegisterPendingResponse register(RegisterRequest request) {
        ensureUniqueUser(request.email(), request.username(), null);
        // Public sign-up creates normal user accounts only; admin provisioning should stay an internal operation.
        if (request.role() != Role.USER) {
            throw new BadRequestException("Self-service registration is limited to USER accounts.");
        }
        if (request.provider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "This starter service accepts local registration only. OAuth users are expected from the configured provider flow."
            );
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setBio(blankToNull(request.bio()));
        user.setProfilePicUrl(blankToNull(request.profilePicUrl()));
        user.setBannerUrl(null);
        user.setRole(request.role());
        user.setProvider(request.provider());
        user.setActive(true);
        user.setPrivateAccount(false);
        user.setEmailVerified(false);
        user.setPendingEmail(null);

        userRepository.save(user);
        EmailOtpService.OtpIssueResult otpIssueResult = emailOtpService.issueOtp(
                user.getUserId(),
                user.getEmail(),
                OtpPurpose.REGISTER,
                "ConnectSphere verification code"
        );
        return new RegisterPendingResponse(
                user.getUserId(),
                user.getEmail(),
                otpIssueResult.expiresAt(),
                otpIssueResult.debugCode()
        );
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        User user = getUserByEmail(request.email());
        if (!user.isActive()) {
            throw new BadRequestException("Account is inactive.");
        }
        if (user.isEmailVerified()) {
            return;
        }
        emailOtpService.verifyOtpOrThrow(request.email(), OtpPurpose.REGISTER, request.code());
        user.setEmailVerified(true);
        userRepository.save(user);
        emailOtpService.purgeExpired();
    }

    @Override
    public RegisterPendingResponse resendRegisterOtp(ResendOtpRequest request) {
        User user = getUserByEmail(request.email());
        if (!user.isActive()) {
            throw new BadRequestException("Account is inactive.");
        }
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified.");
        }
        EmailOtpService.OtpIssueResult otpIssueResult = emailOtpService.issueOtp(
                user.getUserId(),
                user.getEmail(),
                OtpPurpose.REGISTER,
                "ConnectSphere verification code"
        );
        return new RegisterPendingResponse(
                user.getUserId(),
                user.getEmail(),
                otpIssueResult.expiresAt(),
                otpIssueResult.debugCode()
        );
    }

    @Override
    public void requestPasswordReset(ForgotPasswordRequest request) {
        if (request == null) {
            return;
        }

        // Avoid user enumeration: always respond the same on the controller.
        User user = userRepository.findByEmail(request.email().trim().toLowerCase()).orElse(null);
        if (user == null) {
            return;
        }
        if (!user.isActive()) {
            return;
        }
        if (user.getProvider() != AuthProvider.LOCAL) {
            return;
        }

        emailOtpService.issueOtp(
                user.getUserId(),
                user.getEmail(),
                OtpPurpose.PASSWORD_RESET,
                "ConnectSphere password reset code"
        );
        emailOtpService.purgeExpired();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid request.");
        }

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Verification code is invalid or expired."));
        if (!user.isActive()) {
            throw new BadRequestException("Verification code is invalid or expired.");
        }
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("Password reset is not available for social login accounts.");
        }

        emailOtpService.verifyOtpOrThrow(request.email(), OtpPurpose.PASSWORD_RESET, request.code());

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must differ from the current password.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        emailOtpService.purgeExpired();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );

        User user = getUserByEmail(request.email());
        if (!user.isActive()) {
            throw new BadCredentialsException("Account is inactive.");
        }
        if (!user.isEmailVerified()) {
            throw new BadCredentialsException("Email is not verified.");
        }
        return buildAuthResponse(user);
    }

    @Override
    public void logout(LogoutRequest request) {
        jwtTokenService.revoke(request.accessToken());
        if (request.refreshToken() != null && !request.refreshToken().isBlank()) {
            jwtTokenService.revoke(request.refreshToken());
        }
        jwtTokenService.purgeExpiredRevocations();
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        boolean valid = jwtTokenService.isAccessTokenUsable(request.token());
        if (!valid) {
            return new TokenValidationResponse(false, null, null, null, null);
        }

        User user = userRepository.findByEmail(jwtTokenService.extractEmail(request.token()))
                .orElseThrow(() -> new NotFoundException("User not found."));
        if (!user.isActive() || !user.isEmailVerified()) {
            return new TokenValidationResponse(false, null, null, null, null);
        }

        return new TokenValidationResponse(
                true,
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                jwtTokenService.extractExpiry(request.token())
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenService.isRefreshTokenUsable(request.refreshToken())) {
            throw new BadRequestException("Refresh token is invalid or expired.");
        }

        String email = jwtTokenService.extractEmail(request.refreshToken());
        User user = getUserByEmail(email);
        if (!user.isActive()) {
            throw new BadRequestException("Inactive accounts cannot refresh tokens.");
        }
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email is not verified.");
        }

        // Rotation keeps the refresh-token lifecycle tidy and makes logout/deactivation safer.
        jwtTokenService.revoke(request.refreshToken());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findByUserId(userId.trim())
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicUserProfileResponse getPublicUserProfile(String userId) {
        User user = getUserById(userId);
        if (!user.isActive()) {
            throw new NotFoundException("User not found.");
        }
        return PublicUserProfileResponse.from(user);
    }

    @Override
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);
        ensureUniqueUser(request.email(), request.username(), user.getUserId());

        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setBio(blankToNull(request.bio()));
        user.setProfilePicUrl(blankToNull(request.profilePicUrl()));
        user.setBannerUrl(blankToNull(request.bannerUrl()));
        user.setPrivateAccount(request.privateAccount());

        return UserProfileResponse.from(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must differ from the current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAllActiveUsersOrdered().stream()
                    .map(UserSummaryResponse::from)
                    .toList();
        }

        return userRepository.searchByUsername(query.trim()).stream()
                .filter(User::isActive)
                .map(UserSummaryResponse::from)
                .toList();
    }

    @Override
    public void deactivateAccount(String email) {
        User user = getUserByEmail(email);
        user.setActive(false);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        JwtTokenService.TokenDetails accessToken = jwtTokenService.generateAccessToken(user);
        JwtTokenService.TokenDetails refreshToken = jwtTokenService.generateRefreshToken(user);
        return new AuthResponse(
                accessToken.token(),
                refreshToken.token(),
                accessToken.expiresAt(),
                refreshToken.expiresAt(),
                UserProfileResponse.from(user)
        );
    }

    private void ensureUniqueUser(String email, String username, String currentUserId) {
        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> !user.getUserId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new BadRequestException("Email is already in use.");
                });
        userRepository.findByUsername(username.trim())
                .filter(user -> !user.getUserId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new BadRequestException("Username is already in use.");
                });
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
