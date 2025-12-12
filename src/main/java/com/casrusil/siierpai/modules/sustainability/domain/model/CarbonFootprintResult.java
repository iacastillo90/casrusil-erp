package com.casrusil.siierpai.modules.sustainability.domain.model;

import java.math.BigDecimal;

/**
 * Resultado del cálculo de huella de carbono para una factura.
 *
 * @param totalCarbonFootprint Huella total en kgCO2e.
 * @param category             Categoría principal identificada.
 * @param confidenceScore      Nivel de confianza de la clasificación (0.0 -
 *                             1.0).
 */
public record CarbonFootprintResult(
        BigDecimal totalCarbonFootprint,
        String category,
        double confidenceScore) {
}
