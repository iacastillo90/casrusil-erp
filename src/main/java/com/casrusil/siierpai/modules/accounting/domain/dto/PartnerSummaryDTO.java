package com.casrusil.siierpai.modules.accounting.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary of financial status for a partner (Customer/Supplier).
 * Optimized for Partner Ledger view.
 */
public record PartnerSummaryDTO(
        String rut,
        String name,
        BigDecimal totalDebt, // Deuda Total
        BigDecimal overdueDebt, // Deuda Vencida (>30 días)
        int pendingInvoices, // Cantidad de documentos
        LocalDate lastMovement) {
}
