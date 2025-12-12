package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para asientos contables.
 * 
 * <p>
 * Persiste la cabecera del asiento contable. Los detalles (líneas)
 * se almacenan en una estructura relacionada o embebida (según diseño).
 * 
 * <p>
 * Incluye metadatos como fecha, glosa general y estado.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "accounting_entries", schema = "accounting")
public class AccountingEntryEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "occurred_on", nullable = false)
    private Instant occurredOn;

    @Column(nullable = false)
    private String description;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "tax_payer_id")
    private String taxPayerId;

    @Column(name = "tax_payer_name")
    private String taxPayerName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "status")
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private com.casrusil.siierpai.modules.accounting.domain.model.EntryType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accounting_entry_lines", schema = "accounting", joinColumns = @JoinColumn(name = "entry_id"))
    private List<AccountingEntryLineEmbeddable> lines = new ArrayList<>();

    public AccountingEntryEntity() {
    }

    public AccountingEntryEntity(UUID id, UUID companyId, Instant occurredOn, String description, String referenceId,
            String referenceType, String taxPayerId, String taxPayerName, String documentType, String documentNumber,
            String status,
            List<AccountingEntryLineEmbeddable> lines,
            com.casrusil.siierpai.modules.accounting.domain.model.EntryType type) {
        this.id = id;
        this.companyId = companyId;
        this.occurredOn = occurredOn;
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.taxPayerId = taxPayerId;
        this.taxPayerName = taxPayerName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.status = status;
        this.lines = lines;
        this.type = type;
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

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public void setOccurredOn(Instant occurredOn) {
        this.occurredOn = occurredOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getTaxPayerId() {
        return taxPayerId;
    }

    public void setTaxPayerId(String taxPayerId) {
        this.taxPayerId = taxPayerId;
    }

    public String getTaxPayerName() {
        return taxPayerName;
    }

    public void setTaxPayerName(String taxPayerName) {
        this.taxPayerName = taxPayerName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public com.casrusil.siierpai.modules.accounting.domain.model.EntryType getType() {
        return type;
    }

    public void setType(com.casrusil.siierpai.modules.accounting.domain.model.EntryType type) {
        this.type = type;
    }

    public List<AccountingEntryLineEmbeddable> getLines() {
        return lines;
    }

    public void setLines(List<AccountingEntryLineEmbeddable> lines) {
        this.lines = lines;
    }
}
