package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa un activo fijo de la empresa.
 * Los activos fijos se deprecian a lo largo de su vida útil.
 */
public class FixedAsset {

    public enum DepreciationMethod {
        LINEAR, // Depreciación lineal
        ACCELERATED // Depreciación acelerada
    }

    private final UUID id;
    private final CompanyId companyId;
    private final String name;
    private final String category; // Ej: "Muebles", "Equipos Computación", "Vehículos"
    private final LocalDate purchaseDate;
    private final BigDecimal purchaseValue;
    private final int usefulLifeYears; // Vida útil en años
    private final DepreciationMethod depreciationMethod;
    private BigDecimal accumulatedDepreciation;

    public FixedAsset(UUID id, CompanyId companyId, String name, String category,
            LocalDate purchaseDate, BigDecimal purchaseValue,
            int usefulLifeYears, DepreciationMethod depreciationMethod) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.purchaseValue = purchaseValue;
        this.usefulLifeYears = usefulLifeYears;
        this.depreciationMethod = depreciationMethod;
        this.accumulatedDepreciation = BigDecimal.ZERO;
    }

    /**
     * Crea un nuevo activo fijo.
     */
    public static FixedAsset create(CompanyId companyId, String name, String category,
            LocalDate purchaseDate, BigDecimal purchaseValue,
            int usefulLifeYears, DepreciationMethod depreciationMethod) {
        return new FixedAsset(UUID.randomUUID(), companyId, name, category,
                purchaseDate, purchaseValue, usefulLifeYears, depreciationMethod);
    }

    /**
     * Registra depreciación acumulada.
     */
    public void addDepreciation(BigDecimal amount) {
        this.accumulatedDepreciation = this.accumulatedDepreciation.add(amount);
    }

    /**
     * Calcula el valor libro actual (valor de compra - depreciación acumulada).
     */
    public BigDecimal getBookValue() {
        return purchaseValue.subtract(accumulatedDepreciation);
    }

    /**
     * Verifica si el activo está completamente depreciado.
     */
    public boolean isFullyDepreciated() {
        return accumulatedDepreciation.compareTo(purchaseValue) >= 0;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public int getUsefulLifeYears() {
        return usefulLifeYears;
    }

    public DepreciationMethod getDepreciationMethod() {
        return depreciationMethod;
    }

    public BigDecimal getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }
}
