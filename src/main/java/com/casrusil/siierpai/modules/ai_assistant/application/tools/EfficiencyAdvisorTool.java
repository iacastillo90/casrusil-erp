package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository.SustainabilityRecordRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tool de LangChain4j para sugerir mejoras de eficiencia basadas en impacto
 * ambiental.
 */
import com.casrusil.siierpai.modules.financial_shield.domain.service.CircularProcurementService;

@Component
public class EfficiencyAdvisorTool {

    private final SustainabilityRecordRepository sustainabilityRepository;
    private final CircularProcurementService circularService;

    public EfficiencyAdvisorTool(SustainabilityRecordRepository sustainabilityRepository,
            CircularProcurementService circularService) {
        this.sustainabilityRepository = sustainabilityRepository;
        this.circularService = circularService;
    }

    @Tool("Sugiere reducción de gastos basada en impacto ambiental analizando los últimos 3 meses")
    public String suggestGreenEfficiencyImprovements() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(3);

        List<Object[]> topCategories = sustainabilityRepository.findTopEmittingCategoriesBetween(start, end);

        if (topCategories.isEmpty()) {
            return "No hay suficientes datos de huella de carbono reciente para generar sugerencias.";
        }

        StringBuilder suggestions = new StringBuilder("Sugerencias de Eficiencia Verde (Basado en últimos 3 meses):\n");

        for (Object[] cat : topCategories) {
            String category = (String) cat[0];

            String alternative = circularService.suggestGreenAlternatives(category);
            if (alternative != null) {
                suggestions.append("  * 💡 OPORTUNIDAD: Compra a '").append(alternative)
                        .append("' (Partner Verde) para reducir tu huella y mejorar tu Green Score.\n");
            }

            if ("Combustible".equalsIgnoreCase(category) || "Transporte".equalsIgnoreCase(category)) {
                suggestions.append("- TRANSPORTE: Se detectó alto impacto en transporte. Considere:\n");
                suggestions.append("  * Planificar rutas logísticas para reducir kilometraje.\n");
                suggestions.append("  * Evaluar proveedores locales para reducir fletes.\n");
                suggestions.append(
                        "  * Mantener vehículos con presión de neumáticos adecuada (ahorra hasta 3% combustible).\n");
            } else if ("Electricidad".equalsIgnoreCase(category) || "Energía".equalsIgnoreCase(category)) {
                suggestions.append("- ENERGÍA: Consumo eléctrico elevado. Considere:\n");
                suggestions.append("  * Reemplazar iluminación por LED.\n");
                suggestions.append("  * Instalar sensores de movimiento en áreas comunes.\n");
                suggestions.append("  * Revisar equipos de climatización.\n");
            } else if ("Papelería".equalsIgnoreCase(category)) {
                suggestions.append("- PAPELERÍA: Alto consumo de papel. Considere:\n");
                suggestions.append("  * Digitalizar facturación y documentación interna.\n");
                suggestions.append("  * Configurar impresoras en modo 'doble cara' por defecto.\n");
            }
        }

        return suggestions.toString();
    }
}
