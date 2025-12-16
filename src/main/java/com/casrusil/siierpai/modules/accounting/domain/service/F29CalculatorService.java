package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.modules.accounting.domain.model.DraftF29;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
    private static final String COSTO_VENTAS = "510101"; // Cost of sales (matches Listener)

    private final AccountingEntryRepository accountingEntryRepository;
    private final FeeReceiptRepository feeReceiptRepository;
    private final AnomalyDetectionService anomalyDetectionService;

    public F29CalculatorService(AccountingEntryRepository accountingEntryRepository,
            FeeReceiptRepository feeReceiptRepository,
            AnomalyDetectionService anomalyDetectionService) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.feeReceiptRepository = feeReceiptRepository;
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
            LocalDate entryDate = entry.getEntryDate();

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

                if (accountCode.equals(IVA_DEBITO_FISCAL)) {
                    // VAT collected on sales (Liability: Credit + / Debit -)
                    // If IS_DEBIT (NC), subtract.
                    vatDebit = vatDebit.add(line.credit()).subtract(line.debit());

                } else if (accountCode.equals(IVA_CREDITO_FISCAL)) {
                    // VAT paid on purchases (Asset: Debit + / Credit -)
                    // If IS_CREDIT (NC), subtract.
                    vatCredit = vatCredit.add(line.debit()).subtract(line.credit());

                } else if (accountCode.startsWith("4")) {
                    // CLASS 4: REVENUE (INGRESOS) (Credit + / Debit -)
                    if (accountCode.equals(VENTAS_EXENTAS)) {
                        totalSalesExempt = totalSalesExempt.add(line.credit()).subtract(line.debit());
                    } else {
                        totalSalesTaxable = totalSalesTaxable.add(line.credit()).subtract(line.debit());
                    }

                } else if (accountCode.startsWith("5")) {
                    // CLASS 5: EXPENSES (GASTOS) (Debit + / Credit -)
                    totalPurchasesTaxable = totalPurchasesTaxable.add(line.debit()).subtract(line.credit());
                }
            }
        }

        // Calculate net VAT payable (positive = pay to SII, negative = recoverable)
        BigDecimal vatPayable = vatDebit.subtract(vatCredit);

        // Sum Fee Retentions for the period
        List<FeeReceipt> fees = feeReceiptRepository.findByCompanyIdAndIssueDateBetween(companyId, start, end);
        BigDecimal feeWithholding = fees.stream()
                .map(FeeReceipt::getRetentionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate final total to pay
        BigDecimal vatToPay = vatPayable.max(BigDecimal.ZERO);
        BigDecimal recoverable = vatPayable.min(BigDecimal.ZERO).abs();
        BigDecimal totalPayable = vatToPay.add(feeWithholding);

        // Generar detalle de líneas SII
        List<F29Report.F29Line> details = new ArrayList<>();

        // Débitos (Ventas)
        if (vatDebit.compareTo(BigDecimal.ZERO) > 0) {
            // Code 538: Débito Fiscal IVA
            details.add(new F29Report.F29Line("538", "Débito Fiscal IVA", vatDebit));
        }
        if (totalSalesTaxable.compareTo(BigDecimal.ZERO) > 0) {
            // Code 503: Ventas Netas
            details.add(new F29Report.F29Line("503", "Ventas Netas", totalSalesTaxable));
        }

        // Créditos (Compras)
        if (vatCredit.compareTo(BigDecimal.ZERO) > 0) {
            // Code 520: Crédito Fiscal IVA
            details.add(new F29Report.F29Line("520", "Crédito Fiscal IVA", vatCredit));
        }

        if (vatToPay.compareTo(BigDecimal.ZERO) > 0) {
            details.add(new F29Report.F29Line("089", "Impuesto Determinado", vatToPay));
        } else {
            // Code 077: Remanente Crédito Fiscal
            details.add(new F29Report.F29Line("077", "Remanente Crédito Fiscal", recoverable));
        }

        if (feeWithholding.compareTo(BigDecimal.ZERO) > 0) {
            details.add(new F29Report.F29Line("151", "Retención Honorarios (13.75%)", feeWithholding));
        }

        return new F29Report(
                period,
                totalSalesTaxable,
                totalSalesExempt,
                totalPurchasesTaxable,
                totalPurchasesExempt,
                vatDebit,
                vatCredit,
                vatToPay,
                recoverable,
                feeWithholding,
                totalPayable,
                details,
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
