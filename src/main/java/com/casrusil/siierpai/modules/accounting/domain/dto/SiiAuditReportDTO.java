package com.casrusil.siierpai.modules.accounting.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Comprehensive report comparing local ERP data with SII RCV data.
 */
public record SiiAuditReportDTO(
        TaxSummary siiTotal,
        TaxSummary erpTotal,
        List<DiscrepancyItem> discrepancies,
        boolean isMatch) {
    public record TaxSummary(
            BigDecimal netAmount,
            BigDecimal iva,
            int documentCount) {
    }

    public record DiscrepancyItem(
            String documentType, // "33", "34", "61"
            Long folio,
            String rutCounterpart,
            LocalDate date,
            BigDecimal amountSii,
            BigDecimal amountErp,
            String type, // MISSING_IN_ERP, MISSING_IN_SII, AMOUNT_MISMATCH
            String severity // CRITICAL, WARNING
    ) {
    }
}
