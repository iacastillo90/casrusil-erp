package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.dto.IncomeStatementReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para generar reportes financieros inteligentes.
 */
@Service
public class ReportingService {

    private final AccountingEntryRepository entryRepo;
    private final AccountRepository accountRepo;
    private final AiFinancialAnalyst aiAnalyst;

    public ReportingService(AccountingEntryRepository entryRepo, AccountRepository accountRepo,
            AiFinancialAnalyst aiAnalyst) {
        this.entryRepo = entryRepo;
        this.accountRepo = accountRepo;
        this.aiAnalyst = aiAnalyst;
    }

    public IncomeStatementReportDTO generateIncomeStatement(int month, int year) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // 1. Obtener asientos del periodo (Ampliado a 3, 4, 5, 6 para capturar Ventas
        // en clase 3)
        List<AccountingEntry> entries = entryRepo.findInPeriodForClasses(companyId, start, end, List.of(3, 4, 5, 6));

        // 2. Cargar Mapa de Cuentas para clasificación inteligente
        Map<String, Account> accountMap = accountRepo.findAll(companyId).stream()
                .collect(Collectors.toMap(Account::getCode, Function.identity()));

        // 3. Calcular Agregados usando AccountType o Inferencia
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal costs = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            for (AccountingEntryLine line : entry.getLines()) {
                String code = line.accountCode();

                // Try to get account from map, but don't fail if missing
                Account account = accountMap.get(code);

                // GOD LEVEL LOGIC: Dynamic Inference if Account is missing
                boolean isRev = (account != null && isRevenue(account)) || code.startsWith("4");
                boolean isExp = (account != null && isExpense(account)) || code.startsWith("5") || code.startsWith("6");

                if (isRev) {
                    // Revenue: Credit - Debit
                    BigDecimal amount = line.credit().subtract(line.debit());
                    revenue = revenue.add(amount);

                } else if (isExp) {
                    // Expense: Debit - Credit
                    BigDecimal amount = line.debit().subtract(line.credit());

                    // COST OF SALES vs EXPENSES
                    // Standard Chilean: 5101xx is Cost of Sales.
                    // Legacy/Anomaly: User says "5.1.xx" might be Admin.
                    // Logic: If it looks like "5101..." (Standard Cost), it's Cost.
                    // dynamic fallback: if it strictly starts with "5.1." (often legacy admin),
                    // verify?
                    // Safest bet: Explicitly map Cost roots.
                    if (code.startsWith("5101") || code.startsWith("5.1.01") || code.equals("COSTO_VENTAS")) {
                        costs = costs.add(amount);
                    } else {
                        // Everything else (Honorarios 52xx, Admin 5.1?, Sales 53xx) -> Operating
                        // Expenses
                        expenses = expenses.add(amount);
                    }
                }
            }
        }

        BigDecimal grossProfit = revenue.subtract(costs);
        BigDecimal netIncome = grossProfit.subtract(expenses);

        // 4. Generar Prompt para la IA (RAG ligero)
        String financialContext = String.format(
                "Ingresos: %s, Costos: %s, Gastos: %s, Utilidad: %s. Mes: %d/%d",
                formatMoney(revenue), formatMoney(costs), formatMoney(expenses), formatMoney(netIncome), month, year);

        // Llamada al módulo AI Assistant
        String analysis = aiAnalyst.generateExecutiveSummary(financialContext);

        return new IncomeStatementReportDTO(
                new IncomeStatementReportDTO.PeriodDTO(month, year),
                revenue, costs, grossProfit, expenses, netIncome,
                calculateMargin(netIncome, revenue),
                buildBreakdown(entries, accountMap, AccountType.REVENUE),
                buildBreakdown(entries, accountMap, AccountType.EXPENSE), // Simplificado: Todo gasto aquí por ahora
                analysis);
    }

    private BigDecimal calculateMargin(BigDecimal netIncome, BigDecimal revenue) {
        if (revenue.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return netIncome.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private List<IncomeStatementReportDTO.CategoryBreakdown> buildBreakdown(
            List<AccountingEntry> entries,
            Map<String, Account> accountMap,
            AccountType targetType) {

        Map<String, BigDecimal> amountsByAccount = new HashMap<>();
        Map<String, String> namesByAccount = new HashMap<>();

        for (AccountingEntry entry : entries) {
            for (AccountingEntryLine line : entry.getLines()) {
                Account account = accountMap.get(line.accountCode());
                if (account == null)
                    continue;

                boolean matches = false;
                if (targetType == AccountType.REVENUE) {
                    matches = isRevenue(account);
                } else if (targetType == AccountType.EXPENSE) {
                    matches = isExpense(account);
                }

                if (!matches)
                    continue;

                BigDecimal amount;
                if (targetType == AccountType.REVENUE) {
                    amount = line.credit().subtract(line.debit());
                } else {
                    amount = line.debit().subtract(line.credit());
                }

                amountsByAccount.merge(line.accountCode(), amount, BigDecimal::add);
                namesByAccount.putIfAbsent(line.accountCode(), line.accountName());
            }
        }

        // Calculate total for percentage based on the specific breakdown sum
        BigDecimal total = amountsByAccount.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return amountsByAccount.entrySet().stream()
                .map(entry -> {
                    double percentage = 0;
                    if (total.compareTo(BigDecimal.ZERO) != 0) {
                        percentage = entry.getValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                    }
                    return new IncomeStatementReportDTO.CategoryBreakdown(
                            namesByAccount.getOrDefault(entry.getKey(), entry.getKey()),
                            entry.getValue(),
                            percentage);
                })
                .sorted((a, b) -> b.amount().compareTo(a.amount())) // Descending
                .collect(Collectors.toList());
    }

    private String formatMoney(BigDecimal amount) {
        return "$" + amount.setScale(0, RoundingMode.HALF_UP).toString();
    }

    /**
     * Determines if an account is considered Revenue.
     * Uses dynamic AccountType or fallback legacy codes (Class 3 Sales).
     */
    private boolean isRevenue(Account account) {
        return account.getType() == AccountType.REVENUE
                || account.getCode().startsWith("3.1.01") // Legacy: Sales in Equity Class
                || account.getCode().startsWith("4"); // Legacy: Standard Revenue Class
    }

    private boolean isExpense(Account account) {
        return account.getType() == AccountType.EXPENSE
                || account.getCode().startsWith("5")
                || account.getCode().startsWith("6");
    }
}
