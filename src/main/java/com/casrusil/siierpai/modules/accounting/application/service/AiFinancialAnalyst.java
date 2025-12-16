package com.casrusil.siierpai.modules.accounting.application.service;

import org.springframework.stereotype.Service;

/**
 * Interfaz para el Analista Financiero basado en IA.
 */
public interface AiFinancialAnalyst {
    /**
     * Genera un resumen ejecutivo basado en el contexto financiero proporcionado.
     * 
     * @param context Contexto financiero en texto (ingresos, costos, etc.)
     * @return El análisis narrativo.
     */
    String generateExecutiveSummary(String context);
}

@Service
class SimpleAiFinancialAnalyst implements AiFinancialAnalyst {
    @Override
    public String generateExecutiveSummary(String context) {
        // En una implementación real, llamaría a ConversationService o
        // ChatLanguageModel.
        // Aquí simulamos para cumplir el contrato y permitir que el código compile y
        // corra.
        return "Resumen IA: " + context
                + ". La empresa muestra un desempeño estable. Se sugiere revisar costos operativos.";
    }
}
