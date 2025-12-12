package com.casrusil.siierpai.modules.accounting.domain.model;

import java.util.List;

/**
 * Representa un borrador del Formulario 29 (Declaración Mensual y Pago
 * Simultáneo).
 * 
 * <p>
 * Este objeto agrupa el reporte generado, las advertencias detectadas por la IA
 * y un indicador de seguridad para su envío. Es el resultado del proceso de
 * cálculo de impuestos mensuales.
 * 
 * <h2>Componentes:</h2>
 * <ul>
 * <li>{@link F29Report}: Los datos calculados del formulario.</li>
 * <li>{@code warnings}: Lista de anomalías detectadas (duplicados, montos
 * sospechosos).</li>
 * <li>{@code isSafeToSend}: Indicador de si la IA recomienda enviar este
 * borrador.</li>
 * </ul>
 * 
 * @param report       El reporte F29 calculado.
 * @param warnings     Lista de advertencias encontradas durante el cálculo.
 * @param isSafeToSend true si no hay advertencias críticas, false en caso
 *                     contrario.
 * 
 * @see F29Report
 * @see AnomalyWarning
 * @since 1.0
 */
public record DraftF29(
                F29Report report,
                List<AnomalyWarning> warnings,
                boolean isSafeToSend) {
}
