package com.casrusil.SII_ERP_AI.modules.accounting.domain.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.F29Report;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.DraftF29;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating Chilean F29 (VAT Declaration).
 * Based on standard Chilean accounting codes and SII regulations.
 */
@Service
public class F29CalculatorService {

    // Chilean Account Codes (from ChileanChartOfAccountsSeeder)
    private static final String IVA_DEBITO_FISCAL = "210401"; // VAT collected on sales
    private static final String IVA_CREDITO_FISCAL = "110801"; // VAT paid on purchases
    private static final String VENTAS_NACIONALES = "410101"; // Taxable sales
    private static final String VENTAS_EXENTAS = "410102"; // Exempt sales
    private static final String COSTO_VENTAS = "5101"; // Cost of sales (purchases proxy)

    private final AccountingEntryRepository accountingEntryRepository;
    private final AnomalyDetectionService anomalyDetectionService;

    public F29CalculatorService(AccountingEntryRepository accountingEntryRepository,
            AnomalyDetectionService anomalyDetectionService) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    /**
     * Calculate F29 VAT declaration for a given period.
     * 
     * @param companyId Company identifier
     * @param period    Year-Month period (e.g., 2025-12)
     * @return F29Report with VAT calculations
     */
    public F29Report calculateF29(CompanyId companyId, YearMonth period) {
        List<AccountingEntry> entries = accountingEntryRepository.findByCompanyId(companyId);

        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        BigDecimal totalSalesTaxable = BigDecimal.ZERO;
        BigDecimal totalSalesExempt = BigDecimal.ZERO;
        BigDecimal totalPurchasesTaxable = BigDecimal.ZERO;
        BigDecimal totalPurchasesExempt = BigDecimal.ZERO;
        BigDecimal vatDebit = BigDecimal.ZERO;
        BigDecimal vatCredit = BigDecimal.ZERO;
        List<String> evidenceIds = new ArrayList<>();

        for (AccountingEntry entry : entries) {
            LocalDate entryDate = entry.getOccurredOn().atZone(ZoneId.systemDefault()).toLocalDate();

            // Filter by period
            if (entryDate.isBefore(start) || entryDate.isAfter(end)) {
                continue;
            }
            String evidence = String.format("Date: %s | Desc: %s | Ref: %s %s | ID: %s",
                    entryDate,
                    entry.getDescription(),
                    entry.getReferenceType() != null ? entry.getReferenceType() : "N/A",
                    entry.getReferenceId() != null ? entry.getReferenceId() : "N/A",
                    entry.getId());
            evidenceIds.add(evidence);

            // Process each line in the entry
            for (AccountingEntryLine line : entry.getLines()) {
                String accountCode = line.accountCode();
                BigDecimal amount = line.credit().add(line.debit()); // Total movement

                switch (accountCode) {
                    case IVA_DEBITO_FISCAL:
                        // VAT collected on sales (credit balance)
                        vatDebit = vatDebit.add(line.credit());
                        break;

                    case IVA_CREDITO_FISCAL:
                        // VAT paid on purchases (debit balance)
                        vatCredit = vatCredit.add(line.debit());
                        break;

                    case VENTAS_NACIONALES:
                        // Taxable sales (credit balance)
                        totalSalesTaxable = totalSalesTaxable.add(line.credit());
                        break;

                    case VENTAS_EXENTAS:
                        // Exempt sales (credit balance)
                        totalSalesExempt = totalSalesExempt.add(line.credit());
                        break;

                    case COSTO_VENTAS:
                        // Purchases approximation (debit balance)
                        // In a real system, you'd have specific purchase accounts
                        totalPurchasesTaxable = totalPurchasesTaxable.add(line.debit());
                        break;

                    default:
                        // Ignore other accounts
                        break;
                }
            }
        }

        // Calculate net VAT payable (positive = pay to SII, negative = recoverable)
        BigDecimal vatPayable = vatDebit.subtract(vatCredit);

        return new F29Report(
                period,
                totalSalesTaxable,
                totalSalesExempt,
                totalPurchasesTaxable,
                totalPurchasesExempt,
                vatDebit,
                vatCredit,
                vatPayable,
                evidenceIds);
    }

    /**
     * Calculate F29 Draft with Anomaly Detection (The Safety Net).
     */
    public DraftF29 calculateDraftF29(CompanyId companyId, YearMonth period) {
        F29Report report = calculateF29(companyId, period);
        List<AnomalyWarning> warnings = anomalyDetectionService.detectAnomalies(report);

        boolean isSafe = warnings.stream().noneMatch(w -> w.severity() == AnomalyWarning.Severity.CRITICAL);

        return new DraftF29(report, warnings, isSafe);
    }

    /**
     * Legacy method for backward compatibility.
     * 
     * @deprecated Use calculateF29() which returns F29Report instead.
     */
    @Deprecated
    public F29Summary calculateF29Legacy(CompanyId companyId, YearMonth period) {
        F29Report report = calculateF29(companyId, period);
        return new F29Summary(
                report.period(),
                report.vatDebit(),
                report.vatCredit(),
                report.vatPayable());
    }

    /**
     * @deprecated Use F29Report instead
     */
    @Deprecated
    public record F29Summary(YearMonth period, BigDecimal vatDebit, BigDecimal vatCredit, BigDecimal vatPayable) {
    }
}
