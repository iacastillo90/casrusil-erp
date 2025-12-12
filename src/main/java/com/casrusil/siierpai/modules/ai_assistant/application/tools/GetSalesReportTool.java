package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import com.casrusil.siierpai.modules.ai_assistant.domain.model.Tool;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Herramienta de IA para generar reportes de ventas.
 * 
 * <p>
 * Permite al asistente virtual consultar y resumir las ventas de un periodo
 * específico,
 * desglosando por totales netos, impuestos y tipos de documentos.
 * 
 * <h2>Uso por la IA:</h2>
 * <ul>
 * <li>Nombre: {@code get_sales_report}</li>
 * <li>Argumentos: Periodo YYYY-MM o "current".</li>
 * <li>Retorno: Texto formateado con el resumen de ventas.</li>
 * </ul>
 * 
 * @see SearchInvoicesUseCase
 * @since 1.0
 */
@Component
public class GetSalesReportTool implements Tool {

    private final SearchInvoicesUseCase searchInvoicesUseCase;

    public GetSalesReportTool(SearchInvoicesUseCase searchInvoicesUseCase) {
        this.searchInvoicesUseCase = searchInvoicesUseCase;
    }

    @Override
    public String name() {
        return "get_sales_report";
    }

    @Override
    public String description() {
        return "Generates a sales report for a given period. Arguments: YYYY-MM (e.g., 2025-12) or 'current' for current month.";
    }

    @Override
    public String execute(String arguments) {
        try {
            YearMonth period;
            if ("current".equalsIgnoreCase(arguments.trim())) {
                period = YearMonth.now();
            } else {
                period = YearMonth.parse(arguments.trim());
            }

            LocalDate startDate = period.atDay(1);
            LocalDate endDate = period.atEndOfMonth();

            List<Invoice> invoices = searchInvoicesUseCase
                    .getInvoicesByCompany(CompanyContext.requireCompanyId())
                    .stream()
                    .filter(inv -> !inv.getDate().isBefore(startDate) && !inv.getDate().isAfter(endDate))
                    .collect(Collectors.toList());

            if (invoices.isEmpty()) {
                return String.format("No sales found for period %s.", period);
            }

            // Calculate totals
            BigDecimal totalNet = invoices.stream()
                    .map(Invoice::getNetAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalTax = invoices.stream()
                    .map(Invoice::getTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalGross = invoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Group by type
            var byType = invoices.stream()
                    .collect(Collectors.groupingBy(
                            inv -> inv.getType().name(),
                            Collectors.counting()));

            StringBuilder report = new StringBuilder();
            report.append(String.format("Sales Report for %s:\n", period));
            report.append(String.format("Total Invoices: %d\n", invoices.size()));
            report.append(String.format("Net Amount: $%s\n", totalNet));
            report.append(String.format("Tax Amount: $%s\n", totalTax));
            report.append(String.format("Gross Amount: $%s\n\n", totalGross));
            report.append("Breakdown by Type:\n");
            byType.forEach((type, count) -> report.append(String.format("- %s: %d invoices\n", type, count)));

            return report.toString();

        } catch (DateTimeParseException e) {
            return "Invalid period format. Please use YYYY-MM (e.g., 2025-12) or 'current'.";
        } catch (Exception e) {
            return "Error generating sales report: " + e.getMessage();
        }
    }
}
