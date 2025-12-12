package com.casrusil.siierpai.modules.banking.domain.model;

import java.util.UUID;

/**
 * Representa el resultado de una conciliación entre una transacción bancaria
 * y un asiento contable.
 */
public class ReconciliationMatch {
    private final UUID bankTransactionId;
    private final UUID accountingEntryId;
    private final double confidenceScore;
    private final String matchReason;
    private final java.time.LocalDate entryDate;
    private final String entryDescription;
    private final java.math.BigDecimal entryAmount;

    public ReconciliationMatch(UUID bankTransactionId, UUID accountingEntryId,
            double confidenceScore, String matchReason,
            java.time.LocalDate entryDate, String entryDescription, java.math.BigDecimal entryAmount) {
        this.bankTransactionId = bankTransactionId;
        this.accountingEntryId = accountingEntryId;
        this.confidenceScore = confidenceScore;
        this.matchReason = matchReason;
        this.entryDate = entryDate;
        this.entryDescription = entryDescription;
        this.entryAmount = entryAmount;
    }

    // Constructor for manual match (backwards compatibility or manual creation)
    public ReconciliationMatch(UUID bankTransactionId, UUID accountingEntryId,
            double confidenceScore, String matchReason) {
        this(bankTransactionId, accountingEntryId, confidenceScore, matchReason, null, null, null);
    }

    public UUID getBankTransactionId() {
        return bankTransactionId;
    }

    public UUID getAccountingEntryId() {
        return accountingEntryId;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public java.time.LocalDate getEntryDate() {
        return entryDate;
    }

    public String getEntryDescription() {
        return entryDescription;
    }

    public java.math.BigDecimal getEntryAmount() {
        return entryAmount;
    }

    public boolean isHighConfidence() {
        return confidenceScore >= 0.8;
    }
}
