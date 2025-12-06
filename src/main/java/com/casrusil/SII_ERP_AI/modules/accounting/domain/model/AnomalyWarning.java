package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

/**
 * Registro que representa una advertencia de anomalía detectada por la IA.
 * 
 * <p>
 * Se utiliza para alertar al usuario sobre inconsistencias contables o
 * fiscales,
 * como facturas duplicadas, montos inusuales o discrepancias en el F29.
 * 
 * <h2>Niveles de severidad:</h2>
 * <ul>
 * <li>{@link Severity#INFO}: Informativo, no requiere acción inmediata.</li>
 * <li>{@link Severity#WARNING}: Potencial problema, se sugiere revisar.</li>
 * <li>{@link Severity#CRITICAL}: Error grave, requiere corrección
 * inmediata.</li>
 * </ul>
 * 
 * @param code     Código único de la anomalía.
 * @param message  Descripción legible para el usuario.
 * @param severity Nivel de gravedad.
 * 
 * @since 1.0
 */
public record AnomalyWarning(String code, String message, Severity severity) {
    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}
