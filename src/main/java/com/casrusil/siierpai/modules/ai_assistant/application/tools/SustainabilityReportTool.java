package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository.SustainabilityRecordRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tool de LangChain4j para que el agente pueda reportar métricas de
 * sostenibilidad.
 */
@Component
public class SustainabilityReportTool {

    private final SustainabilityRecordRepository sustainabilityRepository;

    public SustainabilityReportTool(SustainabilityRecordRepository sustainabilityRepository) {
        this.sustainabilityRepository = sustainabilityRepository;
    }

    @Tool("Genera un reporte de huella de carbono estimado basado en compras para un mes específico")
    public String getCarbonFootprintReport(int year, int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalFootprint = sustainabilityRepository.sumCarbonFootprintBetween(start, end);
        if (totalFootprint == null)
            totalFootprint = BigDecimal.ZERO;

        List<Object[]> topCategories = sustainabilityRepository.findTopEmittingCategoriesBetween(start, end);

        StringBuilder report = new StringBuilder();
        report.append(String.format("Reporte de Sostenibilidad para %s:\n", targetMonth));
        report.append(String.format("- Huella Total Estimada: %.2f kgCO2e\n", totalFootprint.doubleValue()));

        if (!topCategories.isEmpty()) {
            report.append("- Categorías con mayores emisiones:\n");
            for (Object[] cat : topCategories) {
                String category = (String) cat[0];
                BigDecimal amount = (BigDecimal) cat[1];
                report.append(String.format("  * %s: %.2f kgCO2e\n", category, amount.doubleValue()));
            }
        } else {
            report.append("- No se detectaron emisiones significativas o no hay datos suficientes.\n");
        }

        return report.toString();
    }
}
