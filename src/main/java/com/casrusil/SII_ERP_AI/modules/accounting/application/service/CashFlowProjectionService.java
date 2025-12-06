package com.casrusil.SII_ERP_AI.modules.accounting.application.service;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para proyectar el flujo de caja futuro.
 * Utiliza datos históricos y facturas pendientes para predecir liquidez.
 */
@Service
public class CashFlowProjectionService {

    private final InvoiceRepository invoiceRepository;

    public CashFlowProjectionService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Proyecta el flujo de caja para los próximos N meses.
     */
    public CashFlowProjection projectCashFlow(CompanyId companyId, int monthsAhead) {
        List<MonthlyProjection> projections = new ArrayList<>();

        // Obtener datos históricos
        List<Invoice> historicalInvoices = invoiceRepository.findByCompanyId(companyId);
        Map<YearMonth, BigDecimal> historicalInflows = calculateHistoricalInflows(historicalInvoices);
        Map<YearMonth, BigDecimal> historicalOutflows = calculateHistoricalOutflows(historicalInvoices);

        // Calcular promedios mensuales
        BigDecimal avgMonthlyInflow = calculateAverage(historicalInflows);
        BigDecimal avgMonthlyOutflow = calculateAverage(historicalOutflows);

        // Proyectar cada mes
        BigDecimal runningBalance = BigDecimal.ZERO; // Asumimos balance inicial 0
        YearMonth currentMonth = YearMonth.now();

        for (int i = 0; i < monthsAhead; i++) {
            YearMonth projectedMonth = currentMonth.plusMonths(i);

            // Usar promedio histórico como proyección
            BigDecimal projectedInflow = avgMonthlyInflow;
            BigDecimal projectedOutflow = avgMonthlyOutflow;
            BigDecimal netCashFlow = projectedInflow.subtract(projectedOutflow);

            runningBalance = runningBalance.add(netCashFlow);

            MonthlyProjection projection = new MonthlyProjection(
                    projectedMonth,
                    projectedInflow,
                    projectedOutflow,
                    netCashFlow,
                    runningBalance);

            projections.add(projection);
        }

        return new CashFlowProjection(projections, avgMonthlyInflow, avgMonthlyOutflow);
    }

    /**
     * Calcula ingresos históricos por mes (facturas emitidas).
     */
    private Map<YearMonth, BigDecimal> calculateHistoricalInflows(List<Invoice> invoices) {
        Map<YearMonth, BigDecimal> inflows = new HashMap<>();

        for (Invoice invoice : invoices) {
            // Considerar solo facturas de venta (emisor = empresa)
            // En una implementación real, necesitaríamos distinguir ventas de compras
            YearMonth month = YearMonth.from(invoice.getDate());
            inflows.merge(month, invoice.getTotalAmount(), BigDecimal::add);
        }

        return inflows;
    }

    /**
     * Calcula egresos históricos por mes (facturas recibidas).
     */
    private Map<YearMonth, BigDecimal> calculateHistoricalOutflows(List<Invoice> invoices) {
        // En una implementación real, esto vendría de facturas de compra
        // Por ahora, estimamos como 70% de los ingresos
        Map<YearMonth, BigDecimal> inflows = calculateHistoricalInflows(invoices);
        Map<YearMonth, BigDecimal> outflows = new HashMap<>();

        for (Map.Entry<YearMonth, BigDecimal> entry : inflows.entrySet()) {
            BigDecimal estimatedOutflow = entry.getValue()
                    .multiply(BigDecimal.valueOf(0.7))
                    .setScale(2, RoundingMode.HALF_UP);
            outflows.put(entry.getKey(), estimatedOutflow);
        }

        return outflows;
    }

    /**
     * Calcula el promedio de un mapa de valores.
     */
    private BigDecimal calculateAverage(Map<YearMonth, BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = values.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Proyección de flujo de caja completa.
     */
    public record CashFlowProjection(
            List<MonthlyProjection> monthlyProjections,
            BigDecimal avgMonthlyInflow,
            BigDecimal avgMonthlyOutflow) {
        public boolean hasNegativeMonths() {
            return monthlyProjections.stream()
                    .anyMatch(p -> p.runningBalance().compareTo(BigDecimal.ZERO) < 0);
        }

        public YearMonth firstNegativeMonth() {
            return monthlyProjections.stream()
                    .filter(p -> p.runningBalance().compareTo(BigDecimal.ZERO) < 0)
                    .map(MonthlyProjection::month)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Proyección mensual individual.
     */
    public record MonthlyProjection(
            YearMonth month,
            BigDecimal projectedInflow,
            BigDecimal projectedOutflow,
            BigDecimal netCashFlow,
            BigDecimal runningBalance) {
    }
}
