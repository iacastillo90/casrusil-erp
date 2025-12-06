package com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a summary of a DTE (Documento Tributario Electrónico)
 * retrieved from the SII RCV (Registro de Compras y Ventas).
 */
public record RcvData(
        Integer tipoDte,
        Long folio,
        String rutEmisor,
        String razonSocialEmisor,
        LocalDate fechaEmision,
        BigDecimal montoTotal,
        BigDecimal montoNeto,
        BigDecimal montoIva,
        String estado) {
}
