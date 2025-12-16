package com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA para usuarios del sistema.
 * 
 * <p>
 * Representa a un operador con acceso al sistema, vinculado a una empresa
 * específica.
 * Maneja credenciales (hash de contraseña) y roles de acceso.
 * 
 * @see UserRole
 * @since 1.0
 */
@Entity
@Table(name = "users", schema = "sso")
public class UserEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferences", schema = "sso", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "pref_key")
    @Column(name = "pref_value")
    private Map<String, String> preferences = new HashMap<>();

    public UserEntity() {
    }

    public UserEntity(UUID id, String email, String fullName, String passwordHash, UserRole role, UUID companyId,
            boolean isActive,
            Instant createdAt, Map<String, String> preferences) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.companyId = companyId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.preferences = preferences;
    }

    // ... existing getters setters .. since we replaced up to 104, careful

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
