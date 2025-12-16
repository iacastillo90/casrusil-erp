package com.casrusil.siierpai.modules.accounting.domain.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO Inteligente para el Estado de Resultados.
 * Incluye análisis narrativo generado por IA.
 */
public record IncomeStatementReportDTO(
        PeriodDTO period,
        BigDecimal totalRevenue,
        BigDecimal totalCostOfSales,
        BigDecimal grossProfit,
        BigDecimal totalOperatingExpenses,
        BigDecimal netIncome,
        BigDecimal netIncomeMargin, // %
        List<CategoryBreakdown> revenueBreakdown, // Detalle por cuenta
        List<CategoryBreakdown> expenseBreakdown, // Detalle por cuenta
        String aiAnalysis // <--- AQUÍ VA LA MAGIA (RAG/LLM)
) {
    public record PeriodDTO(int month, int year) {
    }

    public record CategoryBreakdown(String accountName, BigDecimal amount, double percentage) {
    }
}
