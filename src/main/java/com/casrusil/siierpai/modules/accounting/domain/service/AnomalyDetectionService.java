package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio responsable de detectar anomalías en los reportes
 * financieros.
 * 
 * <p>
 * Analiza el borrador del Formulario 29 y aplica reglas de negocio para
 * identificar
 * situaciones inusuales o potencialmente erróneas antes de la declaración.
 * 
 * <h2>Reglas aplicadas:</h2>
 * <ul>
 * <li><strong>Margen Negativo:</strong> Detecta si el IVA Crédito supera al
 * Débito (acumulación de crédito fiscal).</li>
 * <li><strong>Volumen de Compras:</strong> Alerta sobre compras inusualmente
 * altas (>$10M).</li>
 * <li><strong>Pico de IVA:</strong> Compara el IVA Débito actual con el
 * promedio histórico (simulado).</li>
 * </ul>
 * 
 * @see AnomalyWarning
 * @see F29Report
 * @since 1.0
 */
@Service
public class AnomalyDetectionService {

    private static final BigDecimal HIGH_PURCHASE_THRESHOLD = new BigDecimal("10000000"); // 10 Million
    private static final BigDecimal VAT_SPIKE_THRESHOLD_PERCENT = new BigDecimal("1.50"); // 50% increase

    public List<AnomalyWarning> detectAnomalies(F29Report currentReport) {
        List<AnomalyWarning> warnings = new ArrayList<>();

        // Rule 1: Negative Margin (VAT Credit > VAT Debit)
        // This means the company bought more than it sold (or has accumulated credit).
        // It's not illegal, but worth checking.
        if (currentReport.vatCredit().compareTo(currentReport.vatDebit()) > 0) {
            warnings.add(new AnomalyWarning(
                    "NEG_MARGIN",
                    "El IVA Crédito supera al IVA Débito. ¿Tuviste más compras que ventas este mes?",
                    AnomalyWarning.Severity.WARNING));
        }

        // Rule 2: High Purchase Volume
        if (currentReport.totalPurchasesTaxable().compareTo(HIGH_PURCHASE_THRESHOLD) > 0) {
            warnings.add(new AnomalyWarning(
                    "HIGH_PURCHASE",
                    "El monto de compras netas supera los $10.000.000. Verifica que todas las facturas sean del giro.",
                    AnomalyWarning.Severity.INFO));
        }

        // Rule 3: VAT Debit Spike (Mocked history comparison)
        // In a real scenario, we would inject a repository to fetch the last 6 months
        // average.
        BigDecimal mockedAverageVatDebit = new BigDecimal("500000"); // Example average
        if (currentReport.vatDebit().compareTo(mockedAverageVatDebit.multiply(VAT_SPIKE_THRESHOLD_PERCENT)) > 0) {
            warnings.add(new AnomalyWarning(
                    "VAT_SPIKE",
                    "El IVA Débito es un 50% mayor al promedio histórico estimado ($500.000).",
                    AnomalyWarning.Severity.WARNING));
        }

        return warnings;
    }
}
