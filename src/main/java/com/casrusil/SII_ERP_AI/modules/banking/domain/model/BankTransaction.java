package com.casrusil.SII_ERP_AI.modules.banking.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa una transacción bancaria obtenida de un extracto bancario.
 * Se utiliza para el proceso de conciliación bancaria.
 */
public class BankTransaction {
    private final UUID id;
    private final CompanyId companyId;
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final String reference;
    private boolean reconciled;
    private UUID reconciledWithEntryId;

    public BankTransaction(UUID id, CompanyId companyId, LocalDate date, String description,
            BigDecimal amount, String reference) {
        this.id = id;
        this.companyId = companyId;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.reference = reference;
        this.reconciled = false;
        this.reconciledWithEntryId = null;
    }

    /**
     * Crea una nueva transacción bancaria.
     */
    public static BankTransaction create(CompanyId companyId, LocalDate date, String description,
            BigDecimal amount, String reference) {
        return new BankTransaction(UUID.randomUUID(), companyId, date, description, amount, reference);
    }

    /**
     * Marca la transacción como conciliada con un asiento contable.
     */
    public void markAsReconciled(UUID accountingEntryId) {
        this.reconciled = true;
        this.reconciledWithEntryId = accountingEntryId;
    }

    /**
     * Desmarca la transacción como conciliada.
     */
    public void unreconcile() {
        this.reconciled = false;
        this.reconciledWithEntryId = null;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public UUID getReconciledWithEntryId() {
        return reconciledWithEntryId;
    }
}
