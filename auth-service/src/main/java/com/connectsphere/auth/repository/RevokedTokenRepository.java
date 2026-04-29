package com.connectsphere.auth.repository;

import com.connectsphere.auth.entity.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    void deleteByExpiresAtBefore(Instant cutoff);
}
