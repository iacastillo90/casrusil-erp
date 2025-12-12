package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import com.casrusil.siierpai.modules.accounting.domain.model.DraftF29;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Tool;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Herramienta de IA para calcular el borrador del Formulario 29.
 * 
 * <p>
 * Permite al asistente virtual calcular los impuestos mensuales bajo demanda
 * basándose en la información contable actual.
 * 
 * <h2>Uso por la IA:</h2>
 * <ul>
 * <li>Nombre: {@code calculate_f29}</li>
 * <li>Argumentos: Periodo en formato YYYY-MM (ej: "2025-12")</li>
 * <li>Retorno: JSON con el borrador del F29 y advertencias.</li>
 * </ul>
 * 
 * @see F29CalculatorService
 * @since 1.0
 */
@Component
public class CalculateF29Tool implements Tool {

    private final F29CalculatorService f29CalculatorService;
    private final ObjectMapper objectMapper;

    public CalculateF29Tool(F29CalculatorService f29CalculatorService, ObjectMapper objectMapper) {
        this.f29CalculatorService = f29CalculatorService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "calculate_f29";
    }

    @Override
    public String description() {
        return "Calculates the F29 (VAT Declaration) for a given period. Arguments: YYYY-MM (e.g., 2025-12)";
    }

    @Override
    public String execute(String arguments) {
        try {
            YearMonth period = YearMonth.parse(arguments.trim());
            DraftF29 draft = f29CalculatorService
                    .calculateDraftF29(CompanyContext.requireCompanyId(), period);

            return objectMapper.writeValueAsString(draft);
        } catch (DateTimeParseException e) {
            return "Invalid period format. Please use YYYY-MM (e.g., 2025-12).";
        } catch (Exception e) {
            return "Error calculating F29: " + e.getMessage();
        }
    }
}
