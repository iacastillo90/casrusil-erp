package com.casrusil.SII_ERP_AI.modules.accounting.application.service;

import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.model.EntryType;
import com.casrusil.SII_ERP_AI.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpeningBalanceService {

    private final AccountingEntryService accountingEntryService;

    public OpeningBalanceService(AccountingEntryService accountingEntryService) {
        this.accountingEntryService = accountingEntryService;
    }

    @Transactional
    public void setOpeningBalance(CompanyId companyId, List<OpeningBalanceItem> items) {
        List<AccountingEntryLine> lines = items.stream()
                .map(item -> new AccountingEntryLine(item.accountCode(), item.debit(), item.credit()))
                .collect(Collectors.toList());

        AccountingEntry entry = new AccountingEntry(
                companyId,
                "Asiento de Apertura / Saldo Inicial",
                "OPENING-BALANCE",
                "OPENING",
                lines,
                EntryType.OPENING);

        accountingEntryService.recordEntry(entry);
    }

    public record OpeningBalanceItem(String accountCode, BigDecimal debit, BigDecimal credit) {
    }
}
