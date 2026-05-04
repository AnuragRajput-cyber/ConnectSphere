package com.connectsphere.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.repository.EmailOtpRepository;
import com.connectsphere.auth.repository.PendingRegistrationRepository;
import com.connectsphere.auth.repository.ReportRepository;
import com.connectsphere.auth.repository.RevokedTokenRepository;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void cleanDatabase() {
        reportRepository.deleteAll();
        revokedTokenRepository.deleteAll();
        emailOtpRepository.deleteAll();
        pendingRegistrationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void swaggerEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("ConnectSphere Auth Service API"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void registerVerifyLoginAndSearchUser() throws Exception {
        String unique = unique("andre");
        String email = unique + "@example.com";

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Andre Coder",
                                  "bio": "Building ConnectSphere",
                                  "profilePicUrl": "https://example.com/andre.png",
                                  "role": "USER",
                                  "provider": "LOCAL"
                                }
                                """.formatted(unique, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.debugOtpCode").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.assertj.core.api.Assertions.assertThat(userRepository.findByEmail(email)).isEmpty();
        org.assertj.core.api.Assertions.assertThat(pendingRegistrationRepository.findByEmail(email)).isPresent();

        String otp = JsonTestHelper.readField(registerResponse, "debugOtpCode");

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s"
                                }
                                """.formatted(email, otp)))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(userRepository.findByEmail(email)).isPresent();
        org.assertj.core.api.Assertions.assertThat(pendingRegistrationRepository.findByEmail(email)).isEmpty();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value(unique));

        String searchResponse = mockMvc.perform(get("/api/v1/auth/search")
                        .param("query", unique.substring(0, 4)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonTestHelper.assertArrayContainsFieldValue(searchResponse, "username", unique);
    }

    @Test
    void loginRefreshAndValidateToken() throws Exception {
        String email = unique("nina") + "@example.com";
        String username = unique("nina");

        String registerResponse = registerAndVerify(username, email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email));

        String refreshToken = JsonTestHelper.readField(registerResponse, "refreshToken");
        String accessToken = JsonTestHelper.readField(registerResponse, "accessToken");

        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "%s"
                                }
                                """.formatted(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void changePasswordDeactivateAndModerateReport() throws Exception {
        String userEmail = unique("sara") + "@example.com";
        String username = unique("sara");
        String authResponse = registerAndVerify(username, userEmail);
        String accessToken = JsonTestHelper.readField(authResponse, "accessToken");

        mockMvc.perform(patch("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content("""
                                {
                                  "currentPassword": "StrongPass123",
                                  "newPassword": "EvenStronger456"
                                }
                                """))
                .andExpect(status().isOk());

        User admin = createAdminUser(unique("admin") + "@example.com", unique("admin"));
        String adminToken = jwtTokenService.generateAccessToken(admin).token();

        User targetUser = userRepository.findByEmail(userEmail).orElseThrow();

        String reportResponse = mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content("""
                                {
                                  "targetType": "USER",
                                  "targetId": "%s",
                                  "reason": "Spam account",
                                  "details": "Testing moderation pipeline"
                                }
                                """.formatted(admin.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String reportId = JsonTestHelper.readField(reportResponse, "reportId");

        mockMvc.perform(get("/api/v1/reports")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportId").value(reportId));

        mockMvc.perform(patch("/api/v1/reports/{reportId}/resolve", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("""
                                {
                                  "action": "DISMISS",
                                  "resolutionNotes": "No admin action needed in test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"));

        mockMvc.perform(patch("/api/v1/auth/deactivate")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account deactivated successfully."));

        userRepository.findByUserId(targetUser.getUserId()).orElseThrow();
    }

    @Test
    void updateProfileAllowsPrivateAccountWithLongOAuthImageUrl() throws Exception {
        String email = unique("oauth") + "@example.com";
        String username = unique("oauth");
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("unused-password"));
        user.setFullName("OAuth User");
        user.setBio("");
        user.setProfilePicUrl(null);
        user.setBannerUrl(null);
        user.setPrivateAccount(false);
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.GOOGLE);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setPendingEmail(null);
        user = userRepository.save(user);

        String accessToken = jwtTokenService.generateAccessToken(user).token();
        String longProfilePicUrl = "https://lh3.googleusercontent.com/a/"
                + "A".repeat(720)
                + "=s256-c";

        mockMvc.perform(put("/api/v1/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "fullName": "OAuth User",
                                  "bio": "",
                                  "profilePicUrl": "%s",
                                  "bannerUrl": "",
                                  "privateAccount": true
                                }
                                """.formatted(username, email, longProfilePicUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.privateAccount").value(true))
                .andExpect(jsonPath("$.profilePicUrl").value(longProfilePicUrl));
    }

    private String registerAndVerify(String username, String email) throws Exception {
        String pendingResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "StrongPass123",
                                  "fullName": "Test User",
                                  "bio": "Integration testing",
                                  "profilePicUrl": "",
                                  "role": "USER",
                                  "provider": "LOCAL"
                                }
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String otp = JsonTestHelper.readField(pendingResponse, "debugOtpCode");

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s"
                                }
                                """.formatted(email, otp)))
                .andExpect(status().isOk());

        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "StrongPass123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private User createAdminUser(String email, String username) {
        User admin = new User();
        admin.setEmail(email);
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        admin.setFullName("Admin User");
        admin.setBio("Admin");
        admin.setProfilePicUrl(null);
        admin.setBannerUrl(null);
        admin.setPrivateAccount(false);
        admin.setRole(Role.ADMIN);
        admin.setProvider(AuthProvider.LOCAL);
        admin.setActive(true);
        admin.setEmailVerified(true);
        admin.setPendingEmail(null);
        return userRepository.save(admin);
    }

    private String unique(String prefix) {
        return prefix + System.nanoTime();
    }
}
