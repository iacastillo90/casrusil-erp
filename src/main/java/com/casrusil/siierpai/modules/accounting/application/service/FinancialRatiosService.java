package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialRatiosService {

    private final AccountingEntryRepository accountingEntryRepository;

    public FinancialRatiosService(AccountingEntryRepository accountingEntryRepository) {
        this.accountingEntryRepository = accountingEntryRepository;
    }

    public LiquidityReport getLiquidityRatios(CompanyId companyId, int year, int month) {
        YearMonth currentPeriod = YearMonth.of(year, month);
        YearMonth previousPeriod = currentPeriod.minusMonths(1);

        // Calcular ratios para el mes actual y el anterior (para ver la variación)
        Ratios current = calculateRatiosForDate(companyId, currentPeriod.atEndOfMonth());
        Ratios previous = calculateRatiosForDate(companyId, previousPeriod.atEndOfMonth());

        return new LiquidityReport(
                current.currentRatio,
                calculateDelta(current.currentRatio, previous.currentRatio),
                current.acidTest,
                calculateDelta(current.acidTest, previous.acidTest),
                current.workingCapital,
                getDaysRunway(current.workingCapital));
    }

    private Ratios calculateRatiosForDate(CompanyId companyId, LocalDate asOfDate) {
        List<AccountingEntry> entries = accountingEntryRepository.findByCompanyId(companyId);

        Map<String, BigDecimal> balances = new HashMap<>();

        // 1. Calcular saldos acumulados a la fecha de corte (Incluyendo TODO el día de
        // corte)
        for (AccountingEntry entry : entries) {
            // CORRECCION: Usar getEntryDate() y comparison !isAfter para incluir el día
            // completo
            if (!entry.getEntryDate().isAfter(asOfDate)) {
                for (AccountingEntryLine line : entry.getLines()) {
                    // Saldo Deudor Base (Debe - Haber)
                    balances.merge(line.accountCode(), line.debit().subtract(line.credit()), BigDecimal::add);
                }
            }
        }

        // 2. Clasificar según Plan de Cuentas
        // Activo Circulante: Todo lo que empieza con 1.1
        BigDecimal currentAssets = sumByPrefix(balances, "1.1");

        // Existencias: Ajustado a 1.1.03 según tu Balance real
        BigDecimal inventory = sumByPrefix(balances, "1.1.03");

        // Pasivo Circulante: Todo lo que empieza con 2.1 (convertir a positivo)
        BigDecimal currentLiabilities = sumByPrefix(balances, "2.1").abs();

        // 3. Protección contra División por Cero y Cálculo de Capital de Trabajo seguro
        if (currentLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            // Si no hay pasivos:
            // - Capital de Trabajo = Activo Circulante - 0 = Activo Circulante (CORRECTO)
            // - Ratios de liquidez: Técnicamente infinitos, usamos valores altos
            // indicativos (999.00) si hay activos.
            BigDecimal safeAssets = currentAssets.compareTo(BigDecimal.ZERO) > 0 ? currentAssets : BigDecimal.ZERO;
            BigDecimal infiniteRatio = safeAssets.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("999.00")
                    : BigDecimal.ZERO;

            return new Ratios(infiniteRatio, infiniteRatio, safeAssets);
        }

        // 4. Cálculos Financieros
        // Razón Corriente = Activo Circulante / Pasivo Circulante
        BigDecimal currentRatio = currentAssets.divide(currentLiabilities, 2, RoundingMode.HALF_UP);

        // Prueba Ácida = (Activo Circulante - Inventario) / Pasivo Circulante
        BigDecimal quickAssets = currentAssets.subtract(inventory);
        BigDecimal acidTest = quickAssets.divide(currentLiabilities, 2, RoundingMode.HALF_UP);

        // Capital de Trabajo = Activo Circulante - Pasivo Circulante
        BigDecimal workingCapital = currentAssets.subtract(currentLiabilities);

        return new Ratios(currentRatio, acidTest, workingCapital);
    }

    private BigDecimal sumByPrefix(Map<String, BigDecimal> balances, String prefix) {
        return balances.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String calculateDelta(BigDecimal current, BigDecimal previous) {
        // Evitar null pointer si es el primer mes
        if (previous == null)
            return "+0.0";
        BigDecimal delta = current.subtract(previous);
        String sign = delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + delta.toPlainString();
    }

    private int getDaysRunway(BigDecimal workingCapital) {
        // Estimación simple: Capital Trabajo / Gasto Diario Promedio (ej: $300.000)
        BigDecimal dailyBurnRate = new BigDecimal("300000");
        if (workingCapital.compareTo(BigDecimal.ZERO) <= 0)
            return 0;
        return workingCapital.divide(dailyBurnRate, 0, RoundingMode.HALF_UP).intValue();
    }

    private record Ratios(BigDecimal currentRatio, BigDecimal acidTest, BigDecimal workingCapital) {
    }

    public record LiquidityReport(
            BigDecimal currentRatio,
            String currentRatioDelta,
            BigDecimal acidTest,
            String acidTestDelta,
            BigDecimal workingCapital,
            int daysRunway) {
    }
}
