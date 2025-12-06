package com.casrusil.SII_ERP_AI.modules.accounting.domain.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
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
            // Fix: entry.getOccurredOn() returns Instant, so we compare directly with
            // another Instant
            if (entry.getOccurredOn().isAfter(asOfDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())) {
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
            }
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
