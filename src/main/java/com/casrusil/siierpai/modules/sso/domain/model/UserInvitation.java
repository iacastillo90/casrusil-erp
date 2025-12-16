package com.casrusil.siierpai.modules.sso.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.Instant;
import java.util.UUID;

/**
 * Invitación para que un usuario se una a una empresa.
 */
public class UserInvitation {
    private final UUID id;
    private final String email;
    private final CompanyId targetCompanyId;
    private final String role; // e.g., "ROLE_USER", "ROLE_ADMIN"
    private final String token;
    private final Instant expiresAt;
    private Status status;

    public enum Status {
        PENDING,
        ACCEPTED,
        EXPIRED,
        REVOKED
    }

    public UserInvitation(UUID id, String email, CompanyId targetCompanyId, String role, String token,
            Instant expiresAt, Status status) {
        this.id = id;
        this.email = email;
        this.targetCompanyId = targetCompanyId;
        this.role = role;
        this.token = token;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public static UserInvitation create(String email, CompanyId companyId, String role) {
        return new UserInvitation(
                UUID.randomUUID(),
                email,
                companyId,
                role,
                UUID.randomUUID().toString(), // Simple token generation
                Instant.now().plusSeconds(86400 * 7), // 7 days expiration
                Status.PENDING);
    }

    public void accept() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Invitation is not pending");
        }
        if (Instant.now().isAfter(expiresAt)) {
            this.status = Status.EXPIRED;
            throw new IllegalStateException("Invitation has expired");
        }
        this.status = Status.ACCEPTED;
    }

    public boolean isValid() {
        return this.status == Status.PENDING && Instant.now().isBefore(expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public CompanyId getTargetCompanyId() {
        return targetCompanyId;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Status getStatus() {
        return status;
    }
}
