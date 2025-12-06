package com.casrusil.SII_ERP_AI.modules.accounting.domain.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.Account;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountingEntryServiceTest {

        @InjectMocks
        private AccountingEntryService accountingEntryService;

        @Mock
        private AccountingEntryRepository accountingEntryRepository;

        @Mock
        private AccountRepository accountRepository;

        @Test
        void recordEntry_ShouldSaveEntry_WhenEntryIsValid() {
                // Given
                CompanyId companyId = CompanyId.random();
                AccountingEntryLine debit = AccountingEntryLine.debit("1101", new BigDecimal("100"));
                AccountingEntryLine credit = AccountingEntryLine.credit("4101", new BigDecimal("100"));

                AccountingEntry entry = new AccountingEntry(
                                companyId,
                                "GLOSS",
                                "REF-001",
                                "MANUAL",
                                List.of(debit, credit),
                                EntryType.NORMAL);

                // Mock accounts to exist and be active
                Account account1 = new Account(UUID.randomUUID(), companyId, "1101", "Cash", AccountType.ASSET,
                                "Cash account",
                                true);
                Account account2 = new Account(UUID.randomUUID(), companyId, "4101", "Sales", AccountType.REVENUE,
                                "Sales account", true);

                when(accountRepository.findByCode(companyId, "1101")).thenReturn(Optional.of(account1));
                when(accountRepository.findByCode(companyId, "4101")).thenReturn(Optional.of(account2));

                // When
                accountingEntryService.recordEntry(entry);

                // Then
                verify(accountingEntryRepository).save(entry);
        }

        @Test
        void recordEntry_ShouldThrowException_WhenAccountDoesNotExist() {
                // Given
                CompanyId companyId = CompanyId.random();
                AccountingEntryLine debit = AccountingEntryLine.debit("1101", new BigDecimal("100"));
                AccountingEntryLine credit = AccountingEntryLine.credit("9999", new BigDecimal("100")); // Non-existent

                AccountingEntry entry = new AccountingEntry(
                                companyId,
                                "GLOSS",
                                "REF-001",
                                "MANUAL",
                                List.of(debit, credit),
                                EntryType.NORMAL);

                Account account1 = new Account(UUID.randomUUID(), companyId, "1101", "Cash", AccountType.ASSET,
                                "Cash account",
                                true);

                when(accountRepository.findByCode(companyId, "1101")).thenReturn(Optional.of(account1));
                when(accountRepository.findByCode(companyId, "9999")).thenReturn(Optional.empty());

                // When/Then
                org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        accountingEntryService.recordEntry(entry);
                });
                verify(accountingEntryRepository, never()).save(any());
        }

        @Test
        void recordEntry_ShouldThrowException_WhenAccountIsInactive() {
                // Given
                CompanyId companyId = CompanyId.random();
                AccountingEntryLine debit = AccountingEntryLine.debit("1101", new BigDecimal("100"));
                AccountingEntryLine credit = AccountingEntryLine.credit("4101", new BigDecimal("100"));

                AccountingEntry entry = new AccountingEntry(
                                companyId,
                                "GLOSS",
                                "REF-001",
                                "MANUAL",
                                List.of(debit, credit),
                                EntryType.NORMAL);

                Account account1 = new Account(UUID.randomUUID(), companyId, "1101", "Cash", AccountType.ASSET,
                                "Cash account",
                                true);
                Account account2 = new Account(UUID.randomUUID(), companyId, "4101", "Sales", AccountType.REVENUE,
                                "Sales account", false); // Inactive

                when(accountRepository.findByCode(companyId, "1101")).thenReturn(Optional.of(account1));
                when(accountRepository.findByCode(companyId, "4101")).thenReturn(Optional.of(account2));

                // When/Then
                org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        accountingEntryService.recordEntry(entry);
                });
                verify(accountingEntryRepository, never()).save(any());
        }
}
