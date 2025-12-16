package com.casrusil.siierpai.modules.accounting.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single line in the Partner Ledger (Invoice or Payment).
 */
public record PartnerMovementDTO(
        String id, // ID interno (UUID)
        LocalDate date,
        String description, // "Factura #123" o "Pago Abono"
        BigDecimal amount, // Positivo=Cargo, Negativo=Abono
        BigDecimal balanceAfter, // Saldo acumulado línea a línea
        String status, // "VENCIDO", "AL DIA", "PAGADO"
        String documentUrl // Link al PDF si existe
) {
}
