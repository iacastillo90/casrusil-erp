package com.casrusil.siierpai.modules.fees.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad raíz del agregado FeeReceipt (Boleta de Honorarios).
 * Refactorizada para cumplir estándares estrictos de nomenclatura.
 */
public class FeeReceipt {

    public enum Status {
        VALID,
        NULLIFIED
    }

    private final UUID id;
    private final CompanyId companyId;
    private final Long folio;
    private final String issuerRut; // Prestador del servicio
    private final String receiverRut; // Empresa receptora
    private final String issuerName; // Nombre del prestador
    private final LocalDate issueDate; // Renombrado de 'date'
    private final BigDecimal grossAmount; // Monto Bruto
    private final BigDecimal retentionAmount; // Retención
    private final BigDecimal netAmount; // Monto Líquido
    private final Status status; // Renombrado de boolean isNullified

    public FeeReceipt(UUID id, CompanyId companyId, Long folio, String issuerRut, String receiverRut, String issuerName,
            LocalDate issueDate, BigDecimal grossAmount, BigDecimal retentionAmount, BigDecimal netAmount,
            Status status) {
        this.id = id;
        this.companyId = companyId;
        this.folio = folio;
        this.issuerRut = issuerRut;
        this.receiverRut = receiverRut;
        this.issuerName = issuerName;
        this.issueDate = issueDate;
        this.grossAmount = grossAmount;
        this.retentionAmount = retentionAmount;
        this.netAmount = netAmount;
        this.status = status;
    }

    public static FeeReceipt create(CompanyId companyId, Long folio, String issuerRut, String receiverRut,
            String issuerName,
            LocalDate issueDate, BigDecimal grossAmount, BigDecimal retentionAmount, BigDecimal netAmount) {
        return new FeeReceipt(UUID.randomUUID(), companyId, folio, issuerRut, receiverRut, issuerName, issueDate,
                grossAmount, retentionAmount, netAmount, Status.VALID);
    }

    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public Long getFolio() {
        return folio;
    }

    public String getIssuerRut() {
        return issuerRut;
    }

    public String getReceiverRut() {
        return receiverRut;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public BigDecimal getRetentionAmount() {
        return retentionAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isNullified() {
        return status == Status.NULLIFIED;
    }
}
