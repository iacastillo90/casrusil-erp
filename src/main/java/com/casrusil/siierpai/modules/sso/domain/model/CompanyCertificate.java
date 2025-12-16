package com.casrusil.siierpai.modules.sso.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "company_certificates", schema = "sso")
public class CompanyCertificate {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "certificate_data", nullable = false)
    private byte[] certificateData;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    public CompanyCertificate() {
    }

    public CompanyCertificate(UUID id, UUID companyId, byte[] certificateData, String password, LocalDate validUntil) {
        this.id = id;
        this.companyId = companyId;
        this.certificateData = certificateData;
        this.password = password;
        this.validUntil = validUntil;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public byte[] getCertificateData() {
        return certificateData;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }
}
