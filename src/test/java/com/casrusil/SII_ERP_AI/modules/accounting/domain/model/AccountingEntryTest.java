package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountingEntryTest {

    @Test
    void shouldCreateBalancedEntry() {
        CompanyId companyId = CompanyId.random();
        AccountingEntryLine debit = AccountingEntryLine.debit("110501", new BigDecimal("1190"));
        AccountingEntryLine credit1 = AccountingEntryLine.credit("310101", new BigDecimal("1000"));
        AccountingEntryLine credit2 = AccountingEntryLine.credit("210401", new BigDecimal("190"));

        AccountingEntry entry = new AccountingEntry(
                companyId,
                "Test Entry",
                UUID.randomUUID().toString(),
                "INVOICE",
                List.of(debit, credit1, credit2),
                EntryType.NORMAL);

        assertNotNull(entry);
        assertEquals(3, entry.getLines().size());
    }

    @Test
    void shouldThrowExceptionWhenEntryIsUnbalanced() {
        CompanyId companyId = CompanyId.random();
        AccountingEntryLine debit = AccountingEntryLine.debit("110501", new BigDecimal("1000"));
        AccountingEntryLine credit = AccountingEntryLine.credit("310101", new BigDecimal("900"));

        assertThrows(IllegalArgumentException.class, () -> {
            new AccountingEntry(
                    companyId,
                    "Unbalanced Entry",
                    UUID.randomUUID().toString(),
                    "INVOICE",
                    List.of(debit, credit),
                    EntryType.NORMAL);
        });
    }

    @Test
    void shouldThrowExceptionWhenDebitOrCreditIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            AccountingEntryLine.debit("110501", new BigDecimal("-100"));
        });
    }
}
