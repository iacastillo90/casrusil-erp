package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accounting_entry_lines", schema = "accounting", joinColumns = @JoinColumn(name = "entry_id"))
    private List<AccountingEntryLineEmbeddable> lines = new ArrayList<>();

    public AccountingEntryEntity() {
    }

    public AccountingEntryEntity(UUID id, UUID companyId, Instant occurredOn, String description, String referenceId,
            String referenceType, List<AccountingEntryLineEmbeddable> lines,
            com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType type) {
        this.id = id;
        this.companyId = companyId;
        this.occurredOn = occurredOn;
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
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

    public com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType getType() {
        return type;
    }

    public void setType(com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType type) {
        this.type = type;
    }

    public List<AccountingEntryLineEmbeddable> getLines() {
        return lines;
    }

    public void setLines(List<AccountingEntryLineEmbeddable> lines) {
        this.lines = lines;
    }
}
