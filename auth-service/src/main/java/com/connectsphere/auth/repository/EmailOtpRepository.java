package com.connectsphere.auth.repository;

import com.connectsphere.auth.entity.EmailOtp;
import com.connectsphere.auth.entity.OtpPurpose;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, String> {

    Optional<EmailOtp> findFirstByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    void deleteByExpiresAtBefore(Instant cutoff);
}

