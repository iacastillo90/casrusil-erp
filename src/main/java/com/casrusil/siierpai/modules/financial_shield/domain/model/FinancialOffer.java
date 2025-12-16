package com.casrusil.siierpai.modules.financial_shield.domain.model;

import java.math.BigDecimal;

/**
 * Oferta de financiamiento generada automáticamente.
 */
public record FinancialOffer(
        String title,
        String description,
        BigDecimal amount,
        String terms, // Ej: "Tasa 0.6% mensual"
        String ctaAction // Acción en el frontend (ej: "ACTION_APPLY_GREEN")
) {
}
