package com.casrusil.SII_ERP_AI.modules.accounting.domain.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.ClosedPeriodRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for closing accounting periods.
 * Implements Chilean accounting standards for monthly closing.
 */
@Service
public class PeriodClosingService {

    // Chilean Account Codes
    private static final String UTILIDAD_EJERCICIO = "3302"; // Current period earnings
    private static final String REVENUE_PREFIX = "4"; // All revenue accounts start with 4
    private static final String EXPENSE_PREFIX = "5"; // All expense accounts start with 5

    private final ClosedPeriodRepository closedPeriodRepository;
    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountingEntryService accountingEntryService;

    public PeriodClosingService(ClosedPeriodRepository closedPeriodRepository,
            AccountingEntryRepository accountingEntryRepository,
            AccountingEntryService accountingEntryService) {
        this.closedPeriodRepository = closedPeriodRepository;
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountingEntryService = accountingEntryService;
    }

    /**
     * Close an accounting period.
     * This will:
     * 1. Calculate profit/loss for the period
     * 2. Create closing entry transferring P&L to equity
     * 3. Lock the period to prevent future modifications
     * 
     * @param companyId Company identifier
     * @param period    Year-Month to close
     * @param userId    User performing the closing
     * @return ClosedPeriod with profit/loss information
     */
    @Transactional
    public ClosedPeriod closePeriod(CompanyId companyId, YearMonth period, UserId userId) {
        // Validate period is not already closed
        if (isPeriodClosed(companyId, period)) {
            throw new IllegalStateException("Period " + period + " is already closed for company " + companyId.value());
        }

        // Calculate profit/loss
        BigDecimal profitLoss = calculateProfitLoss(companyId, period);

        // Create closing entry
        createClosingEntry(companyId, period, profitLoss);

        // Save closed period
        ClosedPeriod closedPeriod = new ClosedPeriod(companyId, period, userId, profitLoss);
        return closedPeriodRepository.save(closedPeriod);
    }

    /**
     * Check if a period is closed.
     */
    public boolean isPeriodClosed(CompanyId companyId, YearMonth period) {
        return closedPeriodRepository.exists(companyId, period);
    }

    /**
     * Validate that a period is open (not closed).
     * Throws exception if period is closed.
     */
    public void validatePeriodOpen(CompanyId companyId, YearMonth period) {
        if (isPeriodClosed(companyId, period)) {
            throw new IllegalStateException("Accounting period " + period + " is closed and cannot be modified.");
        }
    }

    /**
     * Get all closed periods for a company.
     */
    public List<ClosedPeriod> getClosedPeriods(CompanyId companyId) {
        return closedPeriodRepository.findByCompanyId(companyId);
    }

    /**
     * Calculate profit/loss for a period.
     * Profit/Loss = Total Revenue - Total Expenses
     */
    private BigDecimal calculateProfitLoss(CompanyId companyId, YearMonth period) {
        List<AccountingEntry> entries = accountingEntryRepository.findByCompanyId(companyId);

        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            LocalDate entryDate = entry.getOccurredOn().atZone(ZoneId.systemDefault()).toLocalDate();

            // Filter by period
            if (entryDate.isBefore(start) || entryDate.isAfter(end)) {
                continue;
            }

            // Sum revenue and expenses
            for (AccountingEntryLine line : entry.getLines()) {
                String accountCode = line.accountCode();

                if (accountCode.startsWith(REVENUE_PREFIX)) {
                    // Revenue accounts have credit balance
                    totalRevenue = totalRevenue.add(line.credit());
                } else if (accountCode.startsWith(EXPENSE_PREFIX)) {
                    // Expense accounts have debit balance
                    totalExpenses = totalExpenses.add(line.debit());
                }
            }
        }

        // Profit/Loss = Revenue - Expenses
        return totalRevenue.subtract(totalExpenses);
    }

    /**
     * Create closing entry that zeros out revenue and expense accounts
     * and transfers the net to equity (Utilidad del Ejercicio).
     */
    private void createClosingEntry(CompanyId companyId, YearMonth period, BigDecimal profitLoss) {
        List<AccountingEntryLine> lines = new ArrayList<>();

        if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
            // Profit: Debit Revenue, Credit Equity
            // In a full implementation, we would debit each individual revenue account
            // For simplicity, we create a single closing entry
            lines.add(AccountingEntryLine.debit("4101", profitLoss)); // Placeholder: should be all revenue accounts
            lines.add(AccountingEntryLine.credit(UTILIDAD_EJERCICIO, profitLoss));
        } else if (profitLoss.compareTo(BigDecimal.ZERO) < 0) {
            // Loss: Debit Equity, Credit Expenses
            BigDecimal loss = profitLoss.abs();
            lines.add(AccountingEntryLine.debit(UTILIDAD_EJERCICIO, loss));
            lines.add(AccountingEntryLine.credit("5101", loss)); // Placeholder: should be all expense accounts
        }
        // If profit/loss is zero, no closing entry needed

        if (!lines.isEmpty()) {
            AccountingEntry closingEntry = new AccountingEntry(
                    companyId,
                    "Closing Entry - " + period,
                    period.toString(),
                    "PERIOD_CLOSING",
                    lines,
                    com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType.CLOSING);

            accountingEntryService.recordEntry(closingEntry);
        }
    }
}
