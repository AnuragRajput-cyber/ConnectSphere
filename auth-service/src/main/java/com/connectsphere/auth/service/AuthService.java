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
import com.connectsphere.auth.entity.User;
import java.util.List;

public interface AuthService {

    RegisterPendingResponse register(RegisterRequest request);

    void verifyEmail(VerifyEmailRequest request);

    RegisterPendingResponse resendRegisterOtp(ResendOtpRequest request);

    void requestPasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    AuthResponse login(LoginRequest request);

    void logout(LogoutRequest request);

    TokenValidationResponse validateToken(TokenValidationRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    User getUserById(String userId);

    User getUserByEmail(String email);

    PublicUserProfileResponse getPublicUserProfile(String userId);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    List<UserSummaryResponse> searchUsers(String query);

    void deactivateAccount(String email);
}
