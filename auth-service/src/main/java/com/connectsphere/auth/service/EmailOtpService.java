package com.connectsphere.auth.service;

import com.connectsphere.auth.config.OtpProperties;
import com.connectsphere.auth.entity.EmailOtp;
import com.connectsphere.auth.entity.OtpPurpose;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.repository.EmailOtpRepository;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailOtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboundEmailService outboundEmailService;
    private final OtpProperties otpProperties;

    public EmailOtpService(
            EmailOtpRepository emailOtpRepository,
            PasswordEncoder passwordEncoder,
            OutboundEmailService outboundEmailService,
            OtpProperties otpProperties
    ) {
        this.emailOtpRepository = emailOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.outboundEmailService = outboundEmailService;
        this.otpProperties = otpProperties;
    }

    public OtpIssueResult issueOtp(String userId, String email, OtpPurpose purpose, String subject) {
        String normalizedEmail = normalizeEmail(email);

        EmailOtp recent = emailOtpRepository.findFirstByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose)
                .orElse(null);
        if (recent != null && recent.getExpiresAt().isAfter(Instant.now())) {
            // Basic anti-spam: force a short wait window between issues per email+purpose.
            if (recent.getCreatedAt().isAfter(Instant.now().minusSeconds(45))) {
                throw new BadRequestException("Please wait a moment before requesting another verification code.");
            }
        }

        String code = generateSixDigitCode();
        EmailOtp otp = new EmailOtp();
        otp.setUserId(userId);
        otp.setEmail(normalizedEmail);
        otp.setPurpose(purpose);
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(Instant.now().plus(otpProperties.ttl()));
        otp.setAttempts(0);
        otp.setUsed(false);
        emailOtpRepository.save(otp);

        outboundEmailService.sendOtpEmail(normalizedEmail, code, subject);
        return new OtpIssueResult(otp.getOtpId(), otp.getExpiresAt(), otpProperties.returnCodeInResponse() ? code : null);
    }

    public void verifyOtpOrThrow(String email, OtpPurpose purpose, String code) {
        String normalizedEmail = normalizeEmail(email);
        EmailOtp otp = emailOtpRepository.findFirstByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose)
                .orElseThrow(() -> new BadRequestException("Verification code is invalid or expired."));

        if (otp.isUsed() || otp.getExpiresAt().isBefore(Instant.now())) {
            otp.setUsed(true);
            emailOtpRepository.save(otp);
            throw new BadRequestException("Verification code is invalid or expired.");
        }

        int attempts = otp.getAttempts() + 1;
        otp.setAttempts(attempts);

        if (attempts > otpProperties.maxAttempts()) {
            otp.setUsed(true);
            emailOtpRepository.save(otp);
            throw new BadRequestException("Too many attempts. Please request a new verification code.");
        }

        boolean matches = passwordEncoder.matches(code == null ? "" : code.trim(), otp.getCodeHash());
        if (!matches) {
            emailOtpRepository.save(otp);
            throw new BadRequestException("Verification code is invalid or expired.");
        }

        otp.setUsed(true);
        emailOtpRepository.save(otp);
    }

    public void purgeExpired() {
        emailOtpRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email must not be blank.");
        }
        return email.trim().toLowerCase();
    }

    private String generateSixDigitCode() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    public record OtpIssueResult(String otpId, Instant expiresAt, String debugCode) {
    }
}
