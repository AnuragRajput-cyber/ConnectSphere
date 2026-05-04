package com.connectsphere.auth.repository;

import com.connectsphere.auth.entity.PendingRegistration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, String> {

    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByEmailIgnoreCase(String email);

    Optional<PendingRegistration> findByUsernameIgnoreCase(String username);

    void deleteByExpiresAtBefore(Instant cutoff);
}
