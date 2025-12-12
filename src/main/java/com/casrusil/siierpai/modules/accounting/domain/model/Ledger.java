package com.casrusil.siierpai.modules.accounting.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Ledger represents a view of accounting entries for a specific account.
 * This is a read model / query object rather than a persisted entity.
 */
public class Ledger {
    private final CompanyId companyId;
    private final String accountCode;
    private final List<AccountingEntry> entries;
    private final Instant fromDate;
    private final Instant toDate;

    public Ledger(CompanyId companyId, String accountCode, List<AccountingEntry> entries, Instant fromDate,
            Instant toDate) {
        this.companyId = companyId;
        this.accountCode = accountCode;
        this.entries = entries;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public List<AccountingEntry> getEntries() {
        return entries;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    /**
     * Calculate the balance for this account based on the entries.
     * For simplicity, this sums all debits and credits for lines matching the
     * account code.
     */
    public BigDecimal calculateBalance() {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            for (AccountingEntryLine line : entry.getLines()) {
                if (line.accountCode().equals(accountCode)) {
                    totalDebit = totalDebit.add(line.debit());
                    totalCredit = totalCredit.add(line.credit());
                }
            }
        }

        return totalDebit.subtract(totalCredit);
    }
}
