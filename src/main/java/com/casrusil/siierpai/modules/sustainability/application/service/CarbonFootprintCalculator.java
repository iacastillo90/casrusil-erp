package com.casrusil.siierpai.modules.sustainability.application.service;

import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceItemEntity;
import com.casrusil.siierpai.modules.sustainability.domain.model.CarbonFootprintResult;
import com.casrusil.siierpai.modules.sustainability.domain.model.EmissionFactor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para calcular la huella de carbono estimada de una factura.
 * Utiliza heurísticas para clasificar ítems y aplicar factores de emisión.
 */
@Service
public class CarbonFootprintCalculator {

    private final Map<String, EmissionFactor> emissionFactors;

    public CarbonFootprintCalculator() {
        this.emissionFactors = new HashMap<>();
        // Factores referenciales aproximados (Alcance 3)
        // Fuente: Factores de emisión referenciales (ej: DEFRA, IPCC) adaptados.
        this.emissionFactors.put("COMBUSTIBLE", new EmissionFactor("FUEL", "Combustible", 2.68, "L")); // Diesel/Gasolina
                                                                                                       // aprox
        this.emissionFactors.put("ELECTRICIDAD", new EmissionFactor("ENERGY", "Electricidad", 0.4, "kWh")); // Factor de
                                                                                                            // red Chile
                                                                                                            // aprox
        this.emissionFactors.put("PAPELERIA", new EmissionFactor("OFFICE", "Papelería", 1.2, "kg"));
        this.emissionFactors.put("TRANSPORTE", new EmissionFactor("TRANSPORT", "Transporte", 0.15, "km")); // Camión
                                                                                                           // ligero
    }

    /**
     * Calcula la huella de carbono para una factura dada.
     *
     * @param invoice La factura a procesar.
     * @return Resultado del cálculo con la huella total y categoría predominante.
     */
    public CarbonFootprintResult calculate(InvoiceEntity invoice) {
        BigDecimal totalFootprint = BigDecimal.ZERO;
        String predominantCategory = "Otros";
        double maxCategoryFootprint = 0.0;
        double totalConfidence = 0.0;
        int itemsCount = 0;

        List<InvoiceItemEntity> items = invoice.getItems();
        if (items == null || items.isEmpty()) {
            return new CarbonFootprintResult(BigDecimal.ZERO, "Sin ítems", 0.0);
        }

        for (InvoiceItemEntity item : items) {
            String sanitizedName = item.getItemName() != null ? item.getItemName().toUpperCase() : "";
            EmissionFactor factor = findFactor(sanitizedName);

            if (factor != null) {
                // Asumimos que la cantidad viene en la unidad correcta por ahora
                // (simplificación)
                // En una versión más avanzada, habría conversión de unidades.
                BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                double itemFootprint = quantity.doubleValue() * factor.factorValue();

                totalFootprint = totalFootprint.add(BigDecimal.valueOf(itemFootprint));

                if (itemFootprint > maxCategoryFootprint) {
                    maxCategoryFootprint = itemFootprint;
                    predominantCategory = factor.name();
                }
                totalConfidence += 0.9; // Alta confianza si encontramos match
            } else {
                totalConfidence += 0.1; // Baja confianza si no match
            }
            itemsCount++;
        }

        double finalConfidence = itemsCount > 0 ? totalConfidence / itemsCount : 0.0;

        return new CarbonFootprintResult(totalFootprint, predominantCategory, finalConfidence);
    }

    private EmissionFactor findFactor(String itemName) {
        if (itemName.contains("GASOLINA") || itemName.contains("DIESEL") || itemName.contains("PETROLEO")
                || itemName.contains("COMBUSTIBLE")) {
            return emissionFactors.get("COMBUSTIBLE");
        }
        if (itemName.contains("ELECTRICIDAD") || itemName.contains("LUZ") || itemName.contains("ENERGIA")) {
            return emissionFactors.get("ELECTRICIDAD");
        }
        if (itemName.contains("PAPEL") || itemName.contains("IMPRESORA") || itemName.contains("ARCHIVADOR")) {
            return emissionFactors.get("PAPELERIA");
        }
        if (itemName.contains("FLETE") || itemName.contains("TRANSPORTE") || itemName.contains("ENVIO")
                || itemName.contains("DESPACHO")) {
            return emissionFactors.get("TRANSPORTE");
        }
        return null;
    }
}
