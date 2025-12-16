package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de dominio para la generación del Balance General (Balance Sheet).
 * 
 * <p>
 * Calcula los saldos de todas las cuentas contables a una fecha específica y
 * estructura el reporte clasificando en Activos, Pasivos y Patrimonio.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Calcular saldos acumulados de cuentas desde el inicio hasta la fecha de
 * corte.</li>
 * <li>Clasificar cuentas según su tipo (ACTIVO, PASIVO, PATRIMONIO).</li>
 * <li>Verificar la ecuación contable:
 * {@code Activo = Pasivo + Patrimonio}.</li>
 * </ul>
 * 
 * <h2>Lógica de cálculo:</h2>
 * <ul>
 * <li><strong>Activos:</strong> Saldo deudor (Débito - Crédito).</li>
 * <li><strong>Pasivos y Patrimonio:</strong> Saldo acreedor (Crédito -
 * Débito).</li>
 * </ul>
 * 
 * @see BalanceSheetReport
 * @see AccountingEntry
 * @since 1.0
 */
@Service
public class BalanceSheetService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountRepository accountRepository;

    public BalanceSheetService(AccountingEntryRepository accountingEntryRepository,
            AccountRepository accountRepository) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountRepository = accountRepository;
    }

    public BalanceSheetReport generateBalanceSheet(CompanyId companyId, LocalDate asOfDate) {
        List<AccountingEntry> entries = accountingEntryRepository.findByCompanyId(companyId);

        Map<String, BigDecimal> balances = new HashMap<>();

        // Calculate raw balances for all accounts
        for (AccountingEntry entry : entries) {
            // Fix: compare LocalDate directly
            if (entry.getEntryDate().isAfter(asOfDate)) {
                continue;
            }

            for (AccountingEntryLine line : entry.getLines()) {
                balances.merge(line.accountCode(), line.debit().subtract(line.credit()), BigDecimal::add);
            }
        }

        Map<String, BigDecimal> assetAccounts = new HashMap<>();
        Map<String, BigDecimal> liabilityAccounts = new HashMap<>();
        Map<String, BigDecimal> equityAccounts = new HashMap<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO; // Para sumar gastos
        BigDecimal totalGain = BigDecimal.ZERO; // Para sumar ingresos

        // Classify balances by account type
        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            String code = entry.getKey();
            BigDecimal rawBalance = entry.getValue(); // Debit - Credit

            Optional<Account> accountOpt = accountRepository.findByCode(companyId, code);
            if (accountOpt.isEmpty())
                continue;

            Account account = accountOpt.get();
            AccountType type = account.getType();

            if (type == AccountType.ASSET) {
                // Assets have Debit balance (positive)
                assetAccounts.put(account.getName(), rawBalance);
                totalAssets = totalAssets.add(rawBalance);
            } else if (type == AccountType.LIABILITY) {
                // Liabilities have Credit balance (negative raw balance, so negate it)
                BigDecimal creditBalance = rawBalance.negate();
                liabilityAccounts.put(account.getName(), creditBalance);
                totalLiabilities = totalLiabilities.add(creditBalance);
            } else if (type == AccountType.EQUITY) {
                // Equity has Credit balance
                BigDecimal creditBalance = rawBalance.negate();
                equityAccounts.put(account.getName(), creditBalance);
                totalEquity = totalEquity.add(creditBalance);
            } else if (type == AccountType.EXPENSE) {
                // Gastos suelen ser saldo deudor (+)
                totalLoss = totalLoss.add(rawBalance);
            } else if (type == AccountType.REVENUE) {
                // Ingresos suelen ser saldo acreedor (-) en rawBalance
                // Sumamos el valor absoluto o raw según tu lógica de signo
                totalGain = totalGain.add(rawBalance);
            }
        }

        // --- 🚀 LA MAGIA FINANCIERA ---
        // Calculamos el Resultado: (Ingresos - Gastos)
        // Nota: Ajusta los signos según como venga 'rawBalance' de tu base de datos
        // Si Ganancia viene negativo (Haber) y Pérdida positivo (Debe):
        // Total Gain (negative) + Total Loss (positive) = Net Result (if negative ->
        // profit, if positive -> loss)
        // Wait, Revenue (Credit) is negative in rawBalance (Debit - Credit).
        // Expense (Debit) is positive in rawBalance.
        // So rawResult = Revenue + Expense.
        // Example: Rev -100, Exp +80. Result = -20 (Credit balance -> Profit).
        // Example: Rev -100, Exp +120. Result = +20 (Debit balance -> Loss).

        BigDecimal rawResult = totalGain.add(totalLoss);

        // Inyectar en Patrimonio
        // If rawResult is negative (Credit > Debit), it's a profit.
        String resultLabel = rawResult.compareTo(BigDecimal.ZERO) < 0 ? "UTILIDAD DEL EJERCICIO"
                : "PÉRDIDA DEL EJERCICIO";

        // We want to display positive number for Equity.
        // If Profit (-20), we negate to show 20.
        // If Loss (+20), we negate to show -20 (reducing equity).
        BigDecimal displayResult = rawResult.negate();

        if (displayResult.compareTo(BigDecimal.ZERO) != 0) {
            equityAccounts.put(resultLabel, displayResult);
            totalEquity = totalEquity.add(displayResult);
        }

        boolean isBalanced = totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0;

        return new BalanceSheetReport(
                asOfDate,
                totalAssets,
                totalLiabilities,
                totalEquity,
                assetAccounts,
                liabilityAccounts,
                equityAccounts,
                isBalanced);
    }
}
