package com.casrusil.siierpai.modules.sustainability.domain.model;

/**
 * Record para mapear categorías de gasto a factores de emisión (kgCO2e/unidad).
 * 
 * @param categoryId  Identificador de la categoría.
 * @param name        Nombre de la categoría (ej: "Combustible Diesel",
 *                    "Electricidad").
 * @param factorValue Valor del factor de emisión.
 * @param unit        Unidad de medida (ej: L, kWh, kg).
 */
public record EmissionFactor(
        String categoryId,
        String name,
        double factorValue,
        String unit) {
}
