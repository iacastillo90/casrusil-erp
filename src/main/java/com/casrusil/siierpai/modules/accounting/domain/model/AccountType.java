package com.casrusil.siierpai.modules.accounting.domain.model;

/**
 * Enumeración que define los tipos principales de cuentas contables.
 * 
 * <p>
 * Basado en la ecuación contable fundamental:
 * {@code Activo = Pasivo + Patrimonio}
 * 
 * <h2>Tipos:</h2>
 * <ul>
 * <li>{@link #ASSET} (Activo): Recursos controlados por la empresa (Caja,
 * Banco).</li>
 * <li>{@link #LIABILITY} (Pasivo): Obligaciones actuales (Proveedores,
 * Préstamos).</li>
 * <li>{@link #EQUITY} (Patrimonio): Parte residual de los activos (Capital,
 * Resultados).</li>
 * <li>{@link #REVENUE} (Ingresos): Incrementos en beneficios económicos
 * (Ventas).</li>
 * <li>{@link #EXPENSE} (Gastos): Decrementos en beneficios económicos (Sueldos,
 * Servicios).</li>
 * </ul>
 * 
 * @since 1.0
 */
public enum AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE
}
