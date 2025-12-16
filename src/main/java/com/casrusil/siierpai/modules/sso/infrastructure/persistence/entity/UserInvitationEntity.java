package com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_invitations", schema = "sso")
public class UserInvitationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(name = "target_company_id", nullable = false)
    private UUID targetCompanyId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserInvitation.Status status;

    public UserInvitationEntity() {
    }

    public UserInvitationEntity(UUID id, String email, UUID targetCompanyId, String role, String token,
            Instant expiresAt, UserInvitation.Status status) {
        this.id = id;
        this.email = email;
        this.targetCompanyId = targetCompanyId;
        this.role = role;
        this.token = token;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getTargetCompanyId() {
        return targetCompanyId;
    }

    public void setTargetCompanyId(UUID targetCompanyId) {
        this.targetCompanyId = targetCompanyId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public UserInvitation.Status getStatus() {
        return status;
    }

    public void setStatus(UserInvitation.Status status) {
        this.status = status;
    }
}
