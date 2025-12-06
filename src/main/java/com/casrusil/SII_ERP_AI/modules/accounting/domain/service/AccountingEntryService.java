package com.casrusil.SII_ERP_AI.modules.accounting.domain.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio de dominio para gestionar asientos contables.
 * Valida reglas de negocio como la existencia de cuentas y su estado activo.
 * La validación de partida doble (Debe == Haber) se realiza en el modelo de
 * dominio.
 */
@Service
public class AccountingEntryService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountRepository accountRepository;

    public AccountingEntryService(AccountingEntryRepository accountingEntryRepository,
            AccountRepository accountRepository) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Registra un nuevo asiento contable en el libro diario.
     * Valida que todas las cuentas involucradas existan y estén activas.
     * 
     * @param entry El asiento contable a registrar
     * @throws IllegalArgumentException Si alguna cuenta no existe o está inactiva
     */
    @Transactional
    public void recordEntry(AccountingEntry entry) {
        // The AccountingEntry constructor already validates the double-entry principle
        // (balanced debits/credits)

        // Validate that all accounts exist and are active
        validateAccounts(entry);

        accountingEntryRepository.save(entry);
    }

    private void validateAccounts(AccountingEntry entry) {
        Set<String> accountCodes = new HashSet<>();
        for (AccountingEntryLine line : entry.getLines()) {
            accountCodes.add(line.accountCode());
        }

        for (String code : accountCodes) {
            Optional<Account> account = accountRepository.findByCode(entry.getCompanyId(), code);
            if (account.isEmpty()) {
                throw new IllegalArgumentException("Account with code " + code + " does not exist");
            }
            if (!account.get().isActive()) {
                throw new IllegalArgumentException("Account with code " + code + " is not active");
            }
        }
    }
}
