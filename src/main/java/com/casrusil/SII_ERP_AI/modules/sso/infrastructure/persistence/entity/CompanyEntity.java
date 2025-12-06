package com.casrusil.SII_ERP_AI.modules.sso.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para empresas (Tenants).
 * 
 * <p>
 * Representa a una organización que utiliza el sistema ERP.
 * Es la raíz del modelo multi-tenant; casi todos los datos se aíslan por
 * {@code company_id}.
 * 
 * <p>
 * Contiene datos legales (RUT, Razón Social) y de estado.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "companies", schema = "sso")
public class CompanyEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String email;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public CompanyEntity() {
    }

    public CompanyEntity(UUID id, String rut, String razonSocial, String email, boolean isActive, Instant createdAt) {
        this.id = id;
        this.rut = rut;
        this.razonSocial = razonSocial;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
