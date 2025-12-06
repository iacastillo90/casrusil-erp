package com.casrusil.SII_ERP_AI.modules.ai_assistant.application.tools;

import com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model.Tool;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Herramienta de IA para buscar facturas específicas.
 * 
 * <p>
 * Permite al asistente virtual buscar facturas en la base de datos de la
 * empresa actual.
 * Útil para responder preguntas como "¿Llegó la factura de Jumbo?".
 * 
 * <h2>Uso por la IA:</h2>
 * <ul>
 * <li>Nombre: {@code search_invoices}</li>
 * <li>Argumentos: Ninguno (por ahora busca todo, filtrado futuro).</li>
 * <li>Retorno: JSON con la lista de facturas encontradas.</li>
 * </ul>
 * 
 * @see SearchInvoicesUseCase
 * @since 1.0
 */
@Component
public class SearchInvoicesTool implements Tool {

    private final SearchInvoicesUseCase searchInvoicesUseCase;
    private final ObjectMapper objectMapper;

    public SearchInvoicesTool(SearchInvoicesUseCase searchInvoicesUseCase, ObjectMapper objectMapper) {
        this.searchInvoicesUseCase = searchInvoicesUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "search_invoices";
    }

    @Override
    public String description() {
        return "Searches for invoices for the current company. No arguments required.";
    }

    @Override
    public String execute(String arguments) {
        // In a real scenario, arguments could be filters (date range, etc.)
        // For now, we return all invoices for the company
        try {
            List<Invoice> invoices = searchInvoicesUseCase.getInvoicesByCompany(CompanyContext.requireCompanyId());
            if (invoices.isEmpty()) {
                return "[]";
            }
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("summary", "Found " + invoices.size() + " invoices.");
            response.put("invoices", invoices);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "Error searching invoices: " + e.getMessage();
        }
    }
}
