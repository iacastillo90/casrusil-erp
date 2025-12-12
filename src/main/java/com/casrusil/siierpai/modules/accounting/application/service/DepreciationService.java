package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.FixedAsset;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para calcular y registrar depreciación de activos fijos.
 */
@Service
public class DepreciationService {

    private final AccountingEntryService accountingEntryService;

    // Códigos de cuenta estándar chilenos
    private static final String DEPRECIATION_EXPENSE_ACCOUNT = "520101"; // Gasto por Depreciación
    private static final String ACCUMULATED_DEPRECIATION_ACCOUNT = "110901"; // Depreciación Acumulada

    public DepreciationService(AccountingEntryService accountingEntryService) {
        this.accountingEntryService = accountingEntryService;
    }

    /**
     * Calcula la depreciación mensual de un activo fijo.
     */
    public BigDecimal calculateMonthlyDepreciation(FixedAsset asset) {
        if (asset

                .isFullyDepreciated()) {
            return BigDecimal.ZERO;
        }

        BigDecimal annualDepreciation = calculateAnnualDepreciation(asset);
        return annualDepreciation.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la depreciación anual según el método.
     */
    private BigDecimal calculateAnnualDepreciation(FixedAsset asset) {
        switch (asset.getDepreciationMethod()) {
            case LINEAR:
                return calculateLinearDepreciation(asset);
            case ACCELERATED:
                return calculateAcceleratedDepreciation(asset);
            default:
                throw new IllegalArgumentException("Unknown depreciation method: " + asset.getDepreciationMethod());
        }
    }

    /**
     * Depreciación lineal: Valor / Vida útil.
     */
    private BigDecimal calculateLinearDepreciation(FixedAsset asset) {
        BigDecimal years = BigDecimal.valueOf(asset.getUsefulLife()).divide(BigDecimal.valueOf(12), 4,
                RoundingMode.HALF_UP);
        if (years.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return asset.getPurchaseValue()
                .divide(years, 2, RoundingMode.HALF_UP);
    }

    /**
     * Depreciación acelerada (método chileno): doble de la tasa lineal.
     */
    private BigDecimal calculateAcceleratedDepreciation(FixedAsset asset) {
        BigDecimal years = BigDecimal.valueOf(asset.getUsefulLife()).divide(BigDecimal.valueOf(12), 4,
                RoundingMode.HALF_UP);
        if (years.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        BigDecimal linearRate = BigDecimal.ONE.divide(years, 4, RoundingMode.HALF_UP);
        BigDecimal acceleratedRate = linearRate.multiply(BigDecimal.valueOf(2));

        BigDecimal remainingValue = asset.getBookValue();
        return remainingValue.multiply(acceleratedRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Registra la depreciación mensual de un activo.
     */
    public void recordMonthlyDepreciation(FixedAsset asset, LocalDate periodDate) {
        BigDecimal monthlyDepreciation = calculateMonthlyDepreciation(asset);

        if (monthlyDepreciation.compareTo(BigDecimal.ZERO) == 0) {
            return; // Ya está completamente depreciado
        }

        // Crear asiento contable
        List<AccountingEntryLine> lines = new ArrayList<>();
        lines.add(AccountingEntryLine.debit(DEPRECIATION_EXPENSE_ACCOUNT, "Depreciation Expense", monthlyDepreciation));
        lines.add(AccountingEntryLine.credit(ACCUMULATED_DEPRECIATION_ACCOUNT, "Accumulated Depreciation",
                monthlyDepreciation));

        AccountingEntry entry = new AccountingEntry(
                asset.getCompanyId(),
                String.format("Depreciación mensual - %s", asset.getName()),
                asset.getId().toString(),
                "DEPRECIATION",
                null,
                null,
                null,
                null,
                "POSTED",
                lines,
                com.casrusil.siierpai.modules.accounting.domain.model.EntryType.ADJUSTMENT);

        accountingEntryService.recordEntry(entry);
        asset.addDepreciation(monthlyDepreciation);
    }

    /**
     * Calcula los meses transcurridos desde la compra.
     */
    public long getMonthsSincePurchase(FixedAsset asset) {
        return ChronoUnit.MONTHS.between(asset.getPurchaseDate(), LocalDate.now());
    }

    /**
     * Calcula el porcentaje de vida útil consumido.
     */
    public double getDepreciationPercentage(FixedAsset asset) {
        if (asset.getPurchaseValue().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return asset.getAccumulatedDepreciation()
                .divide(asset.getPurchaseValue(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
