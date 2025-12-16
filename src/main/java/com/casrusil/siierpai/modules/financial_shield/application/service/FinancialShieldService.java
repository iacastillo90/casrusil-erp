package com.casrusil.siierpai.modules.financial_shield.application.service;

import com.casrusil.siierpai.modules.accounting.application.service.CashFlowProjectionService;
import com.casrusil.siierpai.modules.financial_shield.domain.model.*;
import com.casrusil.siierpai.modules.financial_shield.domain.service.GreenCreditScoringService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * El "Cerebro" del Escudo Financiero.
 * Orquesta la proyección de caja, el análisis de riesgo y la generación de
 * ofertas verdes.
 */
@Service
public class FinancialShieldService {

    private final CashFlowProjectionService cashFlowProjectionService;
    private final GreenCreditScoringService greenCreditScoringService;

    public FinancialShieldService(CashFlowProjectionService cashFlowProjectionService,
            GreenCreditScoringService greenCreditScoringService) {
        this.cashFlowProjectionService = cashFlowProjectionService;
        this.greenCreditScoringService = greenCreditScoringService;
    }

    public CashFlowHealth analyzeHealth(CompanyId companyId) {
        // 1. Proyección de Flujo de Caja (3 meses)
        var projection = cashFlowProjectionService.projectCashFlow(companyId, 3);

        // Obtener saldo mínimo proyectado (el punto más bajo de la caja)
        BigDecimal minBalance = projection.monthlyProjections().stream()
                .map(p -> p.runningBalance())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        YearMonth firstNegative = projection.firstNegativeMonth();

        // 2. Obtener Score Verde (Pasaporte)
        GreenScore greenScore = greenCreditScoringService.calculateScore(companyId);

        List<FinancialOffer> offers = new ArrayList<>();
        CashFlowHealth.HealthStatus status = CashFlowHealth.HealthStatus.HEALTHY;

        // 3. Árbol de Decisión de Financiamiento
        if (minBalance.compareTo(BigDecimal.ZERO) < 0) {
            status = CashFlowHealth.HealthStatus.CRITICAL;
            BigDecimal deficit = minBalance.abs();

            if (greenScore.isEligibleForGreenCredit()) {
                // ✅ RUTA PREFERENTE (Verde) - El sueño de CORFO
                offers.add(new FinancialOffer(
                        "🌱 Crédito Verde CORFO (Pre-Aprobado)",
                        "Tu Pasaporte Verde te permite acceder a tasas preferenciales.",
                        deficit,
                        "Tasa 0.6% mensual (Ahorro estimado: $150.000)",
                        "ACTION_APPLY_GREEN"));
            } else {
                // ⚠️ RUTA TRADICIONAL
                offers.add(new FinancialOffer(
                        "Factoring Estándar",
                        "Financiamiento inmediato para cubrir déficit de caja.",
                        deficit,
                        "Tasa 1.5% mensual",
                        "ACTION_FACTORING"));
            }
        } else if (minBalance.compareTo(new BigDecimal("5000000")) > 0) {
            // 💰 OPORTUNIDAD DE INVERSIÓN (Tu validación de "al revés")
            offers.add(new FinancialOffer(
                    "Inversión Verde (Fondos ESG)",
                    "Rentabiliza tu excedente de caja apoyando proyectos sostenibles.",
                    minBalance.multiply(new BigDecimal("0.5")), // Invertir 50% del excedente
                    "Retorno esperado 0.5% mensual",
                    "ACTION_INVEST"));
        }

        return new CashFlowHealth(
                status,
                minBalance,
                firstNegative != null ? firstNegative.atDay(1) : null,
                new ArrayList<>(), // Risk factors (TODO: Integrar con API Poder Judicial/Dicom)
                greenScore,
                offers);
    }
}
