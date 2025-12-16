package com.casrusil.siierpai.modules.partners.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "partners", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "company_id", "rut" })
})
public class PartnerEntity {
    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String rut;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "partner_types", joinColumns = @JoinColumn(name = "partner_id"))
    @Column(name = "type")
    private Set<String> types = new HashSet<>();

    public PartnerEntity() {
    }

    public PartnerEntity(UUID id, UUID companyId, String rut, String name, Set<String> types) {
        this.id = id;
        this.companyId = companyId;
        this.rut = rut;
        this.name = name;
        this.types = types;
    }

    // Getters and Setters
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

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }
}
