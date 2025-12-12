package com.casrusil.siierpai.modules.treasury.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.treasury.domain.model.CashTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CashManagementService {

    private final AccountingEntryService accountingEntryService;

    public CashManagementService(AccountingEntryService accountingEntryService) {
        this.accountingEntryService = accountingEntryService;
    }

    @Transactional
    public void recordCashTransaction(CashTransaction transaction) {
        // 1. Save transaction (omitted for brevity, assuming repository exists or just
        // handling accounting)
        // Ideally we should have a CashTransactionRepository.

        // 2. Create Accounting Entry
        createAccountingEntry(transaction);
    }

    private void createAccountingEntry(CashTransaction transaction) {
        List<AccountingEntryLine> lines = new ArrayList<>();
        String cashAccount = "110101"; // Caja

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Cash IN (Debit Cash, Credit Category/Revenue)
            lines.add(AccountingEntryLine.debit(cashAccount, "Cash Deposit", transaction.getAmount()));
            // Determine credit account based on category
            String creditAccount = determineAccountFromCategory(transaction.getCategory(), false);
            lines.add(AccountingEntryLine.credit(creditAccount, "Revenue/Category", transaction.getAmount()));
        } else {
            // Cash OUT (Debit Category/Expense, Credit Cash)
            BigDecimal amount = transaction.getAmount().abs();
            String debitAccount = determineAccountFromCategory(transaction.getCategory(), true);
            lines.add(AccountingEntryLine.debit(debitAccount, "Expense/Category", amount));
            lines.add(AccountingEntryLine.credit(cashAccount, "Cash Withdrawal", amount));
        }

        AccountingEntry entry = new AccountingEntry(
                transaction.getCompanyId(),
                transaction.getDescription(),
                transaction.getId().toString(),
                "CASH",
                null,
                null,
                null,
                null,
                "POSTED",
                lines,
                EntryType.NORMAL);

        accountingEntryService.recordEntry(entry);
    }

    private String determineAccountFromCategory(String category, boolean isExpense) {
        // Simple mapping logic
        if ("SALES".equalsIgnoreCase(category))
            return "310101"; // Ventas
        if ("OFFICE_SUPPLIES".equalsIgnoreCase(category))
            return "510102"; // Gastos de Oficina
        if ("SERVICES".equalsIgnoreCase(category))
            return "510103"; // Servicios
        return isExpense ? "510199" : "310199"; // Other Expense / Other Revenue
    }
}
