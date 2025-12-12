package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpeningBalanceService {

    private final AccountingEntryService accountingEntryService;
    private final AccountRepository accountRepository;

    public OpeningBalanceService(AccountingEntryService accountingEntryService, AccountRepository accountRepository) {
        this.accountingEntryService = accountingEntryService;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void setOpeningBalance(CompanyId companyId, List<OpeningBalanceItem> items) {
        // 1. Crear cuentas faltantes automáticamente
        for (OpeningBalanceItem item : items) {
            ensureAccountExists(companyId, item);
        }

        // 2. Crear líneas del asiento
        List<AccountingEntryLine> lines = items.stream()
                .map(item -> new AccountingEntryLine(
                        item.accountCode(),
                        item.accountName() != null && !item.accountName().isBlank() ? item.accountName()
                                : "Cuenta Importada " + item.accountCode(),
                        item.debit(),
                        item.credit()))
                .collect(Collectors.toList());

        // 3. Registrar el asiento
        AccountingEntry entry = new AccountingEntry(
                companyId,
                "Asiento de Apertura (Importado)",
                "OPENING-BALANCE",
                "OPENING",
                null, // taxPayerId
                null, // taxPayerName
                null, // documentType
                null, // documentNumber
                "POSTED", // status
                lines,
                EntryType.OPENING);

        accountingEntryService.recordEntry(entry);
    }

    private void ensureAccountExists(CompanyId companyId, OpeningBalanceItem item) {
        // Si la cuenta NO existe, la creamos
        if (accountRepository.findByCode(companyId, item.accountCode()).isEmpty()) {
            AccountType type = inferTypeFromCode(item.accountCode());

            // Usamos el nombre que viene del archivo, o uno genérico si viene vacío
            String name = (item.accountName() != null && !item.accountName().isBlank())
                    ? item.accountName()
                    : "Cuenta Importada " + item.accountCode();

            Account newAccount = new Account(
                    companyId,
                    item.accountCode(),
                    name,
                    type,
                    "Auto-generada por Carga de Balance");
            accountRepository.save(newAccount);
        }
    }

    private AccountType inferTypeFromCode(String code) {
        if (code == null || code.isEmpty())
            return AccountType.ASSET;
        char firstDigit = code.charAt(0);
        // Lógica simple para deducir tipo según primer dígito (Estándar chileno)
        return switch (firstDigit) {
            case '1' -> AccountType.ASSET; // Activos
            case '2' -> AccountType.LIABILITY; // Pasivos
            case '3' -> AccountType.EQUITY; // Patrimonio
            case '4' -> AccountType.REVENUE; // Ganancias (Ventas)
            case '5' -> AccountType.EXPENSE; // Pérdidas (Gastos)
            case '6' -> AccountType.EXPENSE; // Costos
            default -> AccountType.ASSET;
        };
    }

    // Actualizamos el DTO (Record) para incluir el nombre
    public record OpeningBalanceItem(String accountCode, String accountName, BigDecimal debit, BigDecimal credit) {
    }
}
