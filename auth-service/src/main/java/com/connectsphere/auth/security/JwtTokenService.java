package com.connectsphere.auth.security;

import com.connectsphere.auth.config.JwtProperties;
import com.connectsphere.auth.entity.RevokedToken;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final RevokedTokenRepository revokedTokenRepository;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties jwtProperties, RevokedTokenRepository revokedTokenRepository) {
        this.jwtProperties = jwtProperties;
        this.revokedTokenRepository = revokedTokenRepository;
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(jwtProperties.secret());
        } catch (IllegalArgumentException ex) {
            keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDetails generateAccessToken(User user) {
        return generateToken(user, "ACCESS", jwtProperties.accessTokenTtl());
    }

    public TokenDetails generateRefreshToken(User user) {
        // Refresh tokens intentionally live longer so clients can rotate access tokens without re-entering credentials.
        return generateToken(user, "REFRESH", jwtProperties.refreshTokenTtl());
    }

    public boolean isAccessTokenUsable(String token) {
        return isTokenUsable(token, "ACCESS");
    }

    public boolean isRefreshTokenUsable(String token) {
        return isTokenUsable(token, "REFRESH");
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw new BadRequestException("Token is invalid or expired.");
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public Instant extractExpiry(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public void revoke(String token) {
        Claims claims = parseClaims(token);
        if (claims.getId() != null) {
            revokedTokenRepository.save(new RevokedToken(claims.getId(), claims.getExpiration().toInstant()));
        }
    }

    public void purgeExpiredRevocations() {
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    private boolean isTokenUsable(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            if (!expectedType.equals(tokenType)) {
                return false;
            }
            String tokenId = claims.getId();
            return tokenId == null || !revokedTokenRepository.existsById(tokenId);
        } catch (BadRequestException ex) {
            return false;
        }
    }

    private TokenDetails generateToken(User user, String tokenType, Duration ttl) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);
        String tokenId = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .id(tokenId)
                .subject(user.getUserId())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "username", user.getUsername(),
                        "role", user.getRole().name(),
                        "provider", user.getProvider().name(),
                        "tokenType", tokenType
                ))
                .signWith(signingKey)
                .compact();

        return new TokenDetails(token, tokenId, expiresAt);
    }

    public record TokenDetails(String token, String tokenId, Instant expiresAt) {
    }
}
