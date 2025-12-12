package com.casrusil.siierpai.modules.accounting.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FixedAsset {

    public enum DepreciationMethod {
        LINEAR,
        ACCELERATED
    }

    private final UUID id;
    private final CompanyId companyId;
    private final String name;
    private final String category;
    private final LocalDate purchaseDate;
    private final BigDecimal purchaseValue;
    private final int usefulLife; // En MESES según requerimiento
    private final BigDecimal residualValue;
    private final LocalDate lastRevaluationDate;
    private final DepreciationMethod depreciationMethod;
    private BigDecimal accumulatedDepreciation;

    public FixedAsset(UUID id, CompanyId companyId, String name, String category,
            LocalDate purchaseDate, BigDecimal purchaseValue,
            int usefulLife, BigDecimal residualValue, LocalDate lastRevaluationDate,
            DepreciationMethod depreciationMethod) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.category = category;
        this.purchaseDate = purchaseDate;
        this.purchaseValue = purchaseValue;
        this.usefulLife = usefulLife;
        this.residualValue = residualValue != null ? residualValue : BigDecimal.ZERO;
        this.lastRevaluationDate = lastRevaluationDate;
        this.depreciationMethod = depreciationMethod;
        this.accumulatedDepreciation = BigDecimal.ZERO;
    }

    public static FixedAsset create(CompanyId companyId, String name, String category,
            LocalDate purchaseDate, BigDecimal purchaseValue,
            int usefulLifeMonths, BigDecimal residualValue,
            DepreciationMethod depreciationMethod) {
        return new FixedAsset(UUID.randomUUID(), companyId, name, category,
                purchaseDate, purchaseValue, usefulLifeMonths, residualValue, null, depreciationMethod);
    }

    public void addDepreciation(BigDecimal amount) {
        this.accumulatedDepreciation = this.accumulatedDepreciation.add(amount);
    }

    public BigDecimal getBookValue() {
        return purchaseValue.subtract(accumulatedDepreciation);
    }

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

    public int getUsefulLife() {
        return usefulLife;
    } // Months

    public BigDecimal getResidualValue() {
        return residualValue;
    }

    public LocalDate getLastRevaluationDate() {
        return lastRevaluationDate;
    }

    public DepreciationMethod getDepreciationMethod() {
        return depreciationMethod;
    }

    public BigDecimal getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }
}
