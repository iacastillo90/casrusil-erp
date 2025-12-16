package com.casrusil.siierpai.modules.accounting.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la tabla de conciliación detallada RCV vs ERP
 */
public record TaxReconciliationDetailDto(
        String id, // ID único para la key de React
        String period, // Ej: "2025-10"
        String documentType, // "Factura Electrónica", "Nota de Crédito"
        Long folio, // Número de documento
        String counterpartRut, // RUT del proveedor/cliente
        String counterpartName, // Razón Social (Lo que pediste)
        BigDecimal amountSii, // Monto según RCV
        BigDecimal amountErp, // Monto según tu base de datos
        String status, // MATCH, MISSING_IN_ERP, MISSING_IN_SII, MISMATCH
        BigDecimal difference // Diferencia numérica
) {
}
