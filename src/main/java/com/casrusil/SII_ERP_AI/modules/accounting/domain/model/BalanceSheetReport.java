package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Reporte inmutable que representa el Balance General de la empresa.
 * 
 * <p>
 * Contiene la foto financiera de la empresa en un momento dado, desglosada
 * en Activos, Pasivos y Patrimonio.
 * 
 * <h2>Componentes:</h2>
 * <ul>
 * <li>Totales calculados para cada grupo.</li>
 * <li>Desglose detallado por cuenta.</li>
 * <li>Indicador {@code isBalanced} que valida la ecuación contable.</li>
 * </ul>
 * 
 * @param date              Fecha de corte del reporte.
 * @param totalAssets       Suma total de activos.
 * @param totalLiabilities  Suma total de pasivos.
 * @param totalEquity       Suma total de patrimonio.
 * @param assetAccounts     Mapa de cuentas de activo y sus saldos.
 * @param liabilityAccounts Mapa de cuentas de pasivo y sus saldos.
 * @param equityAccounts    Mapa de cuentas de patrimonio y sus saldos.
 * @param isBalanced        true si Activo = Pasivo + Patrimonio.
 * 
 * @since 1.0
 */
public record BalanceSheetReport(
                LocalDate date,
                BigDecimal totalAssets,
                BigDecimal totalLiabilities,
                BigDecimal totalEquity,
                Map<String, BigDecimal> assetAccounts,
                Map<String, BigDecimal> liabilityAccounts,
                Map<String, BigDecimal> equityAccounts,
                boolean isBalanced) {
}
