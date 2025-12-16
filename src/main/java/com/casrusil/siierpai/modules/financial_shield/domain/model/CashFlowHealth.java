package com.casrusil.siierpai.modules.financial_shield.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Diagnóstico completo de salud financiera + sostenibilidad.
 * Este objeto alimenta el Widget del Dashboard.
 */
public record CashFlowHealth(
        HealthStatus status,
        BigDecimal projectedBalance30Days,
        LocalDate runwayEnd, // Fecha estimada de quiebre de caja
        List<RiskFactor> riskFactors,
        GreenScore greenScore, // El "Pasaporte"
        List<FinancialOffer> offers // Las soluciones (Verdes o Tradicionales)
) {
    public enum HealthStatus {
        HEALTHY, WARNING, CRITICAL
    }
}
