package com.casrusil.SII_ERP_AI.modules.banking.domain.model;

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

    public ReconciliationMatch(UUID bankTransactionId, UUID accountingEntryId,
            double confidenceScore, String matchReason) {
        this.bankTransactionId = bankTransactionId;
        this.accountingEntryId = accountingEntryId;
        this.confidenceScore = confidenceScore;
        this.matchReason = matchReason;
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

    public boolean isHighConfidence() {
        return confidenceScore >= 0.8;
    }
}
