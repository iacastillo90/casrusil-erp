package com.casrusil.siierpai.modules.accounting.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.UUID;

/**
 * Representa una cuenta contable en el plan de cuentas (Entidad de Dominio).
 * Define el código, nombre y tipo de cuenta (Activo, Pasivo, etc.).
 */
public class Account {
    private final UUID id;
    private final CompanyId companyId;
    private final String code;
    private final String name;
    private final AccountType type;
    private final String description;
    private final boolean active;

    public Account(CompanyId companyId, String code, String name, AccountType type, String description) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.code = code;
        this.name = name;
        this.type = type;
        this.description = description;
        this.active = true;
    }

    public Account(UUID id, CompanyId companyId, String code, String name, AccountType type, String description,
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

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
