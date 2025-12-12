package com.casrusil.siierpai.modules.fees.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fee_receipts", schema = "fees")
public class FeeReceiptEntity {

    public enum Status {
        VALID,
        NULLIFIED
    }

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "folio", nullable = false)
    private Long folio;

    @Column(name = "issuer_rut", nullable = false)
    private String issuerRut;

    @Column(name = "receiver_rut", nullable = false)
    private String receiverRut;

    @Column(name = "issuer_name")
    private String issuerName;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "gross_amount", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "retention_amount", nullable = false)
    private BigDecimal retentionAmount;

    @Column(name = "net_amount", nullable = false)
    private BigDecimal netAmount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public FeeReceiptEntity() {
    }

    public FeeReceiptEntity(UUID id, UUID companyId, Long folio, String issuerRut, String receiverRut,
            String issuerName,
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

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
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
}
