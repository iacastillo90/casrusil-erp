package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.test.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AccountRepository using Testcontainers.
 * Tests actual database operations with PostgreSQL.
 */
@Transactional
class AccountRepositoryIntegrationTest extends BaseIntegrationTest {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountRepositoryIntegrationTest(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Test
    void shouldSaveAndRetrieveAccount() {
        // Given
        CompanyId companyId = CompanyId.random();
        Account account = new Account(
                companyId,
                "1101",
                "Caja",
                AccountType.ASSET,
                "Efectivo en caja");

        // When
        Account saved = accountRepository.save(account);

        // Then
        assertNotNull(saved.getId());
        assertEquals("1101", saved.getCode());
        assertEquals("Caja", saved.getName());
        assertEquals(AccountType.ASSET, saved.getType());
        assertTrue(saved.isActive());
    }

    @Test
    void shouldFindAccountByCode() {
        // Given
        CompanyId companyId = CompanyId.random();
        Account account = new Account(
                companyId,
                "2101",
                "Proveedores",
                AccountType.LIABILITY,
                "Cuentas por pagar");
        accountRepository.save(account);

        // When
        Optional<Account> found = accountRepository.findByCode(companyId, "2101");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Proveedores", found.get().getName());
    }

    @Test
    void shouldFindAllAccountsByCompany() {
        // Given
        CompanyId companyId = CompanyId.random();

        Account account1 = new Account(companyId, "1101", "Caja", AccountType.ASSET, "Cash");
        Account account2 = new Account(companyId, "4101", "Ventas", AccountType.REVENUE, "Sales");

        accountRepository.save(account1);
        accountRepository.save(account2);

        // When
        List<Account> accounts = accountRepository.findAll(companyId);

        // Then
        assertEquals(2, accounts.size());
    }

    @Test
    void shouldNotFindAccountFromDifferentCompany() {
        // Given
        CompanyId company1 = CompanyId.random();
        CompanyId company2 = CompanyId.random();

        Account account = new Account(company1, "1101", "Caja", AccountType.ASSET, "Cash");
        accountRepository.save(account);

        // When
        Optional<Account> found = accountRepository.findByCode(company2, "1101");

        // Then
        assertFalse(found.isPresent(), "Should not find account from different company");
    }
}
