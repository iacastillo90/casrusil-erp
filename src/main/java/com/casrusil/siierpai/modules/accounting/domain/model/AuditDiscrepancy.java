package com.casrusil.siierpai.modules.accounting.domain.model;

import java.math.BigDecimal;

public record AuditDiscrepancy(
        String rut,
        Integer docType,
        Long folio,
        java.time.LocalDate date,
        String counterpart,
        AuditStatus status,
        String description,
        BigDecimal siiAmount,
        BigDecimal erpAmount) {
    public enum AuditStatus {
        OK, MONTO_INCORRECTO, NO_EN_ERP, NO_EN_SII
    }
}
