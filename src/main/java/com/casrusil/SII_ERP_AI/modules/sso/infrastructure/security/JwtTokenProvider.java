package com.casrusil.SII_ERP_AI.modules.sso.infrastructure.security;

import com.casrusil.SII_ERP_AI.modules.sso.domain.model.UserRole;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret:defaultSecretKeyMustBeLongEnoughToBeSecureAndAtLeast256Bits}") String secret,
            @Value("${jwt.expiration:3600000}") long expirationMillis) {
        if ("defaultSecretKeyMustBeLongEnoughToBeSecureAndAtLeast256Bits".equals(secret)) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate safe key if default
        } else {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        }
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserId userId, CompanyId companyId, UserRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMillis);

        return Jwts.builder()
                .setSubject(userId.getValue().toString())
                .claim("companyId", companyId.getValue().toString())
                .claim("role", role.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public UserId getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return new UserId(UUID.fromString(claims.getSubject()));
    }

    public CompanyId getCompanyIdFromToken(String token) {
        Claims claims = getClaims(token);
        return new CompanyId(UUID.fromString(claims.get("companyId", String.class)));
    }
}
