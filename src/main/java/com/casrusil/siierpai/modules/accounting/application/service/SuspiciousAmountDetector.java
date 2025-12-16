package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AuditAlert;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para detectar montos sospechosos usando análisis estadístico
 * híbrido.
 * Combina análisis específico por cliente/proveedor con benchmarks globales.
 */
@Service
public class SuspiciousAmountDetector {

    private final InvoiceRepository invoiceRepository;
    private static final double STANDARD_DEVIATIONS_THRESHOLD = 3.0;
    // Formateador para pesos chilenos
    private static final NumberFormat CLP_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

    public SuspiciousAmountDetector(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Detecta facturas con montos sospechosos.
     * Estrategia Híbrida:
     * 1. Si el cliente/proveedor tiene historia suficiente (>=5), compara contra su
     * propia historia.
     * 2. Si es nuevo o tiene poca historia, compara contra el promedio global de
     * Ventas/Compras.
     */
    public List<AuditAlert> detectSuspiciousAmounts(CompanyId companyId) {
        List<AuditAlert> alerts = new ArrayList<>();
        List<Invoice> allInvoices = invoiceRepository.findByCompanyId(companyId);

        // 1. Calcular Estadísticas Globales (Fallback)
        List<Invoice> allSales = allInvoices.stream()
                .filter(i -> i.getTransactionType() == TransactionType.SALE)
                .toList();
        List<Invoice> allPurchases = allInvoices.stream()
                .filter(i -> i.getTransactionType() == TransactionType.PURCHASE)
                .toList();

        Statistics globalSalesStats = calculateStatistics(allSales);
        Statistics globalPurchaseStats = calculateStatistics(allPurchases);

        // 2. Agrupar por Contraparte
        Map<String, List<Invoice>> invoicesByCounterparty = groupByCounterparty(allInvoices);

        // 3. Analizar cada grupo
        for (Map.Entry<String, List<Invoice>> entry : invoicesByCounterparty.entrySet()) {
            List<Invoice> counterpartyInvoices = entry.getValue();
            if (counterpartyInvoices.isEmpty())
                continue;

            boolean isSaleGroup = counterpartyInvoices.get(0).getTransactionType() == TransactionType.SALE;

            // Decisión: Usar Estadísticas Locales (Específicas) o Globales
            boolean hasEnoughHistory = counterpartyInvoices.size() >= 5;
            Statistics statsToUse;
            String analysisContext; // "Local" o "Global"

            if (hasEnoughHistory) {
                statsToUse = calculateStatistics(counterpartyInvoices);
                analysisContext = "Local";
            } else {
                statsToUse = isSaleGroup ? globalSalesStats : globalPurchaseStats;
                analysisContext = "Global";
            }

            // Si la desviación estándar es 0 (todos los montos iguales) o stats inválidas,
            // saltar
            if (statsToUse.stdDev().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // Detectar outliers
            for (Invoice invoice : counterpartyInvoices) {
                double zScore = calculateZScore(invoice.getTotalAmount(), statsToUse);

                if (Math.abs(zScore) > STANDARD_DEVIATIONS_THRESHOLD) {
                    AuditAlert alert = createSuspiciousAmountAlert(invoice, statsToUse, zScore, analysisContext);
                    alerts.add(alert);
                }
            }
        }

        // También detectar montos redondos sospechosos
        alerts.addAll(detectRoundAmounts(allInvoices));

        return alerts;
    }

    private Map<String, List<Invoice>> groupByCounterparty(List<Invoice> invoices) {
        Map<String, List<Invoice>> grouped = new HashMap<>();
        for (Invoice invoice : invoices) {
            String key;
            if (invoice.getTransactionType() == TransactionType.SALE) {
                key = invoice.getReceiverRut(); // El cliente
            } else {
                key = invoice.getIssuerRut(); // El proveedor
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(invoice);
        }
        return grouped;
    }

    private Statistics calculateStatistics(List<Invoice> invoices) {
        if (invoices.isEmpty())
            return new Statistics(BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal sum = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            sum = sum.add(invoice.getTotalAmount());
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(invoices.size()), 2, RoundingMode.HALF_UP);

        BigDecimal varianceSum = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            BigDecimal diff = invoice.getTotalAmount().subtract(mean);
            varianceSum = varianceSum.add(diff.multiply(diff));
        }
        BigDecimal variance = varianceSum.divide(BigDecimal.valueOf(invoices.size()), 2, RoundingMode.HALF_UP);
        double stdDev = Math.sqrt(variance.doubleValue());

        return new Statistics(mean, BigDecimal.valueOf(stdDev));
    }

    private double calculateZScore(BigDecimal amount, Statistics stats) {
        if (stats.stdDev().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal diff = amount.subtract(stats.mean());
        return diff.divide(stats.stdDev(), 4, RoundingMode.HALF_UP).doubleValue();
    }

    private List<AuditAlert> detectRoundAmounts(List<Invoice> invoices) {
        List<AuditAlert> alerts = new ArrayList<>();

        for (Invoice invoice : invoices) {
            BigDecimal amount = invoice.getTotalAmount();
            // Evitar falsos positivos en montos muy bajos
            if (amount.compareTo(BigDecimal.valueOf(10000)) < 0)
                continue;

            if (amount.remainder(BigDecimal.valueOf(1000000)).compareTo(BigDecimal.ZERO) == 0 ||
                    amount.remainder(BigDecimal.valueOf(100000)).compareTo(BigDecimal.ZERO) == 0) {

                String title = "Monto Redondo Sospechoso";
                String description = String.format(
                        "La factura %s tiene un monto exactamente redondo: %s. " +
                                "Esto podría indicar una estimación o error de digitación.",
                        invoice.getFolio(),
                        CLP_FORMAT.format(amount));

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

    private AuditAlert createSuspiciousAmountAlert(Invoice invoice, Statistics stats, double zScore,
            String analysisContext) {
        boolean isSale = invoice.getTransactionType() == TransactionType.SALE;
        String entityType = isSale ? "del cliente" : "del proveedor";

        String entityRut = isSale ? invoice.getReceiverRut() : invoice.getIssuerRut();
        String businessName = invoice.getBusinessName();
        if (businessName == null || businessName.isBlank() || "Unknown".equalsIgnoreCase(businessName)) {
            businessName = "Sin Razón Social";
        }

        String formattedAmount = CLP_FORMAT.format(invoice.getTotalAmount());
        String formattedMean = CLP_FORMAT.format(stats.mean());

        String title = "Monto Inusual Detectado";

        // Construir mensaje según contexto (Local vs Global)
        String comparisonBase;
        if ("Local".equals(analysisContext)) {
            comparisonBase = String.format("histórico (%s) para este %s", formattedMean,
                    isSale ? "cliente" : "proveedor");
        } else {
            comparisonBase = String.format("global (%s) de todas las %s", formattedMean, isSale ? "ventas" : "compras");
        }

        String description = String.format(
                "La factura %s %s %s (%s) presenta un monto atípico de %s.\n" +
                        "Este valor se aleja %.1f desviaciones estándar del promedio %s.",
                invoice.getFolio(),
                entityType,
                entityRut,
                businessName,
                formattedAmount,
                Math.abs(zScore),
                comparisonBase);

        AuditAlert.Severity severity = Math.abs(zScore) > 4.0
                ? AuditAlert.Severity.CRITICAL
                : AuditAlert.Severity.WARNING;

        return new AuditAlert(
                AuditAlert.Type.SUSPICIOUS_AMOUNT,
                severity,
                title,
                description,
                invoice.getId().toString(),
                "Verificar validez de la transacción. Posible error de digitación o venta excepcional.");
    }

    private record Statistics(BigDecimal mean, BigDecimal stdDev) {
    }
}
