package com.casrusil.siierpai.modules.partners.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Partner {
    private final UUID id;
    private final CompanyId companyId;
    private final String rut;
    private String name;
    private final Set<PartnerType> types; // CUSTOMER, SUPPLIER

    public Partner(UUID id, CompanyId companyId, String rut, String name, Set<PartnerType> types) {
        this.id = id;
        this.companyId = companyId;
        this.rut = rut;
        this.name = name;
        this.types = types != null ? new HashSet<>(types) : new HashSet<>();
    }

    public static Partner create(CompanyId companyId, String rut, String name) {
        return new Partner(UUID.randomUUID(), companyId, rut, name, new HashSet<>());
    }

    public void addType(PartnerType type) {
        this.types.add(type);
    }

    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty() && !name.equals("Unknown") && !name.equals("Sin Nombre")) {
            this.name = name;
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getRut() {
        return rut;
    }

    public String getName() {
        return name;
    }

    public Set<PartnerType> getTypes() {
        return new HashSet<>(types);
    }
}
