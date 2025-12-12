package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AuditAlert;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para detectar montos sospechosos usando análisis estadístico.
 * Identifica facturas con montos inusuales basándose en patrones históricos.
 */
@Service
public class SuspiciousAmountDetector {

    private final InvoiceRepository invoiceRepository;
    private static final double STANDARD_DEVIATIONS_THRESHOLD = 3.0;

    public SuspiciousAmountDetector(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Detecta facturas con montos sospechosos.
     * Usa análisis estadístico por proveedor para identificar outliers.
     */
    public List<AuditAlert> detectSuspiciousAmounts(CompanyId companyId) {
        List<AuditAlert> alerts = new ArrayList<>();
        List<Invoice> allInvoices = invoiceRepository.findByCompanyId(companyId);

        // Agrupar facturas por emisor
        Map<String, List<Invoice>> invoicesByIssuer = groupByIssuer(allInvoices);

        // Analizar cada grupo
        for (Map.Entry<String, List<Invoice>> entry : invoicesByIssuer.entrySet()) {
            String issuerRut = entry.getKey();
            List<Invoice> issuerInvoices = entry.getValue();

            // Necesitamos al menos 5 facturas para análisis estadístico
            if (issuerInvoices.size() < 5) {
                continue;
            }

            Statistics stats = calculateStatistics(issuerInvoices);

            // Detectar outliers
            for (Invoice invoice : issuerInvoices) {
                double zScore = calculateZScore(invoice.getTotalAmount(), stats);

                if (Math.abs(zScore) > STANDARD_DEVIATIONS_THRESHOLD) {
                    AuditAlert alert = createSuspiciousAmountAlert(invoice, stats, zScore);
                    alerts.add(alert);
                }
            }
        }

        // También detectar montos redondos sospechosos (ej: exactamente $1,000,000)
        alerts.addAll(detectRoundAmounts(allInvoices));

        return alerts;
    }

    /**
     * Agrupa facturas por emisor.
     */
    private Map<String, List<Invoice>> groupByIssuer(List<Invoice> invoices) {
        Map<String, List<Invoice>> grouped = new HashMap<>();
        for (Invoice invoice : invoices) {
            grouped.computeIfAbsent(invoice.getIssuerRut(), k -> new ArrayList<>()).add(invoice);
        }
        return grouped;
    }

    /**
     * Calcula estadísticas (media y desviación estándar) de montos.
     */
    private Statistics calculateStatistics(List<Invoice> invoices) {
        // Calcular media
        BigDecimal sum = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            sum = sum.add(invoice.getTotalAmount());
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(invoices.size()), 2, RoundingMode.HALF_UP);

        // Calcular desviación estándar
        BigDecimal varianceSum = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            BigDecimal diff = invoice.getTotalAmount().subtract(mean);
            varianceSum = varianceSum.add(diff.multiply(diff));
        }
        BigDecimal variance = varianceSum.divide(BigDecimal.valueOf(invoices.size()), 2, RoundingMode.HALF_UP);
        double stdDev = Math.sqrt(variance.doubleValue());

        return new Statistics(mean, BigDecimal.valueOf(stdDev));
    }

    /**
     * Calcula el Z-score (número de desviaciones estándar desde la media).
     */
    private double calculateZScore(BigDecimal amount, Statistics stats) {
        if (stats.stdDev().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal diff = amount.subtract(stats.mean());
        return diff.divide(stats.stdDev(), 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Detecta montos redondos sospechosos.
     */
    private List<AuditAlert> detectRoundAmounts(List<Invoice> invoices) {
        List<AuditAlert> alerts = new ArrayList<>();

        for (Invoice invoice : invoices) {
            BigDecimal amount = invoice.getTotalAmount();

            // Verificar si es un número redondo (múltiplo de 100,000 o 1,000,000)
            if (amount.remainder(BigDecimal.valueOf(1000000)).compareTo(BigDecimal.ZERO) == 0 ||
                    amount.remainder(BigDecimal.valueOf(100000)).compareTo(BigDecimal.ZERO) == 0) {

                String title = "Monto Redondo Sospechoso";
                String description = String.format(
                        "La factura %s tiene un monto exactamente redondo: %s. " +
                                "Esto podría indicar una estimación o error de digitación.",
                        invoice.getFolio(),
                        amount);

                AuditAlert alert = new AuditAlert(
                        AuditAlert.Type.SUSPICIOUS_AMOUNT,
                        AuditAlert.Severity.WARNING,
                        title,
                        description,
                        invoice.getId().toString(),
                        "Verificar que el monto sea correcto y no una estimación.");

                alerts.add(alert);
            }
        }

        return alerts;
    }

    /**
     * Crea una alerta de monto sospechoso.
     */
    private AuditAlert createSuspiciousAmountAlert(Invoice invoice, Statistics stats, double zScore) {
        String title = "Monto Inusual Detectado";
        String description = String.format(
                "La factura %s del proveedor %s tiene un monto inusual: %s. " +
                        "Este monto está %.1f desviaciones estándar alejado del promedio (%s). " +
                        "Desviación estándar: %s",
                invoice.getFolio(),
                invoice.getIssuerRut(),
                invoice.getTotalAmount(),
                Math.abs(zScore),
                stats.mean(),
                stats.stdDev());

        AuditAlert.Severity severity = Math.abs(zScore) > 4.0
                ? AuditAlert.Severity.CRITICAL
                : AuditAlert.Severity.WARNING;

        String suggestedAction = "Verificar que el monto sea correcto. Podría ser un error de digitación o una transacción excepcional legítima.";

        return new AuditAlert(
                AuditAlert.Type.SUSPICIOUS_AMOUNT,
                severity,
                title,
                description,
                invoice.getId().toString(),
                suggestedAction);
    }

    /**
     * Record para almacenar estadísticas.
     */
    private record Statistics(BigDecimal mean, BigDecimal stdDev) {
    }
}
