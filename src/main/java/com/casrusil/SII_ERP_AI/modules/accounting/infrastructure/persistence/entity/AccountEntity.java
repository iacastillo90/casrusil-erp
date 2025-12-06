package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountType;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad JPA para cuentas contables.
 * 
 * <p>
 * Representa una cuenta del plan de cuentas de la empresa.
 * Mapea la estructura jerárquica y los atributos de control.
 * 
 * <p>
 * Atributos principales:
 * <ul>
 * <li>Código (ej. 1.1.01)</li>
 * <li>Nombre</li>
 * <li>Tipo (Activo, Pasivo, etc.)</li>
 * </ul>
 * 
 * @since 1.0
 */
@Entity
@Table(name = "accounts", schema = "accounting")
public class AccountEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    private String description;

    @Column(nullable = false)
    private boolean active;

    public AccountEntity() {
    }

    public AccountEntity(UUID id, UUID companyId, String code, String name, AccountType type, String description,
            boolean active) {
        this.id = id;
        this.companyId = companyId;
        this.code = code;
        this.name = name;
        this.type = type;
        this.description = description;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
