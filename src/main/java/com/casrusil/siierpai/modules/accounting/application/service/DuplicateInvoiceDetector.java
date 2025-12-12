package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AuditAlert;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para detectar facturas duplicadas.
 * Utiliza múltiples criterios para identificar posibles duplicados.
 */
@Service
public class DuplicateInvoiceDetector {

    private final InvoiceRepository invoiceRepository;

    public DuplicateInvoiceDetector(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Detecta facturas potencialmente duplicadas para una empresa.
     * 
     * Criterios de duplicación:
     * - Mismo RUT emisor/receptor
     * - Mismo folio
     * - Fecha similar (±3 días)
     * - Monto similar (±5%)
     */
    public List<AuditAlert> detectDuplicates(CompanyId companyId) {
        List<AuditAlert> alerts = new ArrayList<>();
        List<Invoice> allInvoices = invoiceRepository.findByCompanyId(companyId);

        for (int i = 0; i < allInvoices.size(); i++) {
            Invoice invoice1 = allInvoices.get(i);

            for (int j = i + 1; j < allInvoices.size(); j++) {
                Invoice invoice2 = allInvoices.get(j);

                double similarity = calculateSimilarity(invoice1, invoice2);

                if (similarity >= 0.8) {
                    AuditAlert alert = createDuplicateAlert(invoice1, invoice2, similarity);
                    alerts.add(alert);
                }
            }
        }

        return alerts;
    }

    /**
     * Calcula el score de similitud entre dos facturas (0.0 - 1.0).
     */
    private double calculateSimilarity(Invoice inv1, Invoice inv2) {
        double score = 0.0;

        // 1. Mismo RUT emisor (30%)
        if (inv1.getIssuerRut().equals(inv2.getIssuerRut())) {
            score += 0.3;
        }

        // 2. Mismo RUT receptor (20%)
        if (inv1.getReceiverRut().equals(inv2.getReceiverRut())) {
            score += 0.2;
        }

        // 3. Mismo folio (25%)
        if (inv1.getFolio().equals(inv2.getFolio())) {
            score += 0.25;
        }

        // 4. Fecha similar - dentro de 3 días (15%)
        long daysDifference = Math.abs(ChronoUnit.DAYS.between(inv1.getDate(), inv2.getDate()));
        if (daysDifference <= 3) {
            double dateScore = 1.0 - (daysDifference / 3.0);
            score += 0.15 * dateScore;
        }

        // 5. Monto similar - dentro del 5% (10%)
        BigDecimal amount1 = inv1.getTotalAmount();
        BigDecimal amount2 = inv2.getTotalAmount();
        BigDecimal difference = amount1.subtract(amount2).abs();
        BigDecimal average = amount1.add(amount2).divide(BigDecimal.valueOf(2));

        if (average.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentDiff = difference.divide(average, 4, java.math.RoundingMode.HALF_UP);
            if (percentDiff.compareTo(BigDecimal.valueOf(0.05)) <= 0) {
                double amountScore = 1.0 - percentDiff.doubleValue() / 0.05;
                score += 0.1 * amountScore;
            }
        }

        return score;
    }

    /**
     * Crea una alerta de duplicado.
     */
    private AuditAlert createDuplicateAlert(Invoice inv1, Invoice inv2, double similarity) {
        String title = "Posible Factura Duplicada";
        String description = String.format(
                "Las facturas %s y %s parecen ser duplicadas (similitud: %.0f%%). " +
                        "Emisor: %s, Receptor: %s, Monto: %s vs %s",
                inv1.getFolio(),
                inv2.getFolio(),
                similarity * 100,
                inv1.getIssuerRut(),
                inv1.getReceiverRut(),
                inv1.getTotalAmount(),
                inv2.getTotalAmount());

        AuditAlert.Severity severity = similarity >= 0.95
                ? AuditAlert.Severity.CRITICAL
                : AuditAlert.Severity.WARNING;

        String suggestedAction = "Revisar ambas facturas y eliminar el duplicado si corresponde.";

        return new AuditAlert(
                AuditAlert.Type.DUPLICATE_INVOICE,
                severity,
                title,
                description,
                inv1.getId().toString(),
                suggestedAction);
    }
}
