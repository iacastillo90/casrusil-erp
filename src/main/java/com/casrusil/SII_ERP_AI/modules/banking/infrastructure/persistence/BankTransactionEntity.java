package com.casrusil.SII_ERP_AI.modules.banking.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad JPA para transacciones bancarias.
 */
/**
 * Entidad JPA para transacciones bancarias.
 * 
 * <p>
 * Representa un movimiento en la cuenta bancaria de la empresa.
 * Se utiliza para la conciliación bancaria automática o manual.
 * 
 * <p>
 * Puede estar conciliada con un asiento contable existente.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "bank_transactions")
public class BankTransactionEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String reference;

    @Column(nullable = false)
    private boolean reconciled = false;

    @Column(name = "reconciled_with_entry_id")
    private UUID reconciledWithEntryId;

    // Constructors
    public BankTransactionEntity() {
    }

    public BankTransactionEntity(UUID id, UUID companyId, LocalDate date, String description,
            BigDecimal amount, String reference, boolean reconciled,
            UUID reconciledWithEntryId) {
        this.id = id;
        this.companyId = companyId;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.reference = reference;
        this.reconciled = reconciled;
        this.reconciledWithEntryId = reconciledWithEntryId;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }

    public UUID getReconciledWithEntryId() {
        return reconciledWithEntryId;
    }

    public void setReconciledWithEntryId(UUID reconciledWithEntryId) {
        this.reconciledWithEntryId = reconciledWithEntryId;
    }
}
