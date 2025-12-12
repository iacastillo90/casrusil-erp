package com.casrusil.siierpai.modules.accounting.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

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
        BigDecimal feeWithholding,
        BigDecimal totalPayable,
        java.util.List<String> evidenceIds) {
    public F29Report {
        if (vatPayable == null) {
            vatPayable = vatDebit.subtract(vatCredit);
        }
        if (feeWithholding == null) {
            feeWithholding = BigDecimal.ZERO;
        }
        if (totalPayable == null) {
            totalPayable = vatPayable.add(feeWithholding); // Simplification: Total = VAT + Fees
        }
    }

    public BigDecimal getTotalSales() {
        return totalSalesTaxable.add(totalSalesExempt);
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchasesTaxable.add(totalPurchasesExempt);
    }
}
