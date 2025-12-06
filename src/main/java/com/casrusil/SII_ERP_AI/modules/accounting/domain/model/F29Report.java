package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

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
        java.util.List<String> evidenceIds) {
    public F29Report {
        if (vatPayable == null) {
            vatPayable = vatDebit.subtract(vatCredit);
        }
    }

    public BigDecimal getTotalSales() {
        return totalSalesTaxable.add(totalSalesExempt);
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchasesTaxable.add(totalPurchasesExempt);
    }
}
