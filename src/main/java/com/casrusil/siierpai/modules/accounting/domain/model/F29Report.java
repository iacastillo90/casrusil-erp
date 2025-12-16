package com.casrusil.siierpai.modules.accounting.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Enhanced F29 Report with all required fields for Chilean VAT declaration.
 */
public record F29Report(
        YearMonth period,
        BigDecimal totalSalesTaxable,
        BigDecimal totalSalesExempt,
        BigDecimal totalPurchasesTaxable,
        BigDecimal totalPurchasesExempt,
        BigDecimal vatDebit,
        BigDecimal vatCredit,
        BigDecimal vatPayable,
        BigDecimal vatRecoverable,
        BigDecimal feeWithholding,
        BigDecimal totalPayable,
        List<F29Line> details,
        List<String> evidenceIds) {

    public F29Report {
        if (vatPayable == null) {
            BigDecimal net = vatDebit.subtract(vatCredit);
            vatPayable = net.max(BigDecimal.ZERO);
            vatRecoverable = net.min(BigDecimal.ZERO).abs();
        } else if (vatRecoverable == null) {
            // If vatPayable was provided but recoverable wasn't?
            // Usually this constructor is called with all fields or compacted.
            // Let's ensure recoverable is not null if we can infer it.
            vatRecoverable = BigDecimal.ZERO;
        }

        if (feeWithholding == null) {
            feeWithholding = BigDecimal.ZERO;
        }
        if (totalPayable == null) {
            totalPayable = vatPayable.add(feeWithholding).subtract(vatRecoverable);
            if (totalPayable.compareTo(BigDecimal.ZERO) < 0)
                totalPayable = BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalSales() {
        return totalSalesTaxable.add(totalSalesExempt);
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchasesTaxable.add(totalPurchasesExempt);
    }

    public record F29Line(String code, String description, BigDecimal amount) {
    }
}
