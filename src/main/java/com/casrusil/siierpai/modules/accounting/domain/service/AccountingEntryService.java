package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

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

    /**
     * Obtiene el Libro Mayor (General Ledger) para una cuenta específica.
     * Calcula el saldo acumulado fila por fila.
     *
     * @param companyId   ID de la empresa
     * @param accountCode Código de la cuenta
     * @param from        Fecha inicio
     * @param to          Fecha fin
     * @return Lista de movimientos con saldo calculado
     */
    public List<AccountMovement> getLedger(com.casrusil.siierpai.shared.domain.valueobject.CompanyId companyId,
            String accountCode, LocalDate from, LocalDate to) {
        List<AccountMovement> rawMovements = accountingEntryRepository.findMovementsByAccount(companyId, accountCode,
                from, to);
        List<AccountMovement> calculatedMovements = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;

        for (AccountMovement move : rawMovements) {
            balance = balance.add(move.debit()).subtract(move.credit());
            calculatedMovements.add(new AccountMovement(
                    move.entryId(),
                    move.date(),
                    move.gloss(),
                    move.debit(),
                    move.credit(),
                    balance));
        }

        return calculatedMovements;
    }
}
