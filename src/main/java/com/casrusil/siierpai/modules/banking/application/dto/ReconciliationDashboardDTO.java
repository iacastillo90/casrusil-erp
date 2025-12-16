package com.casrusil.siierpai.modules.banking.application.dto;

import com.casrusil.siierpai.modules.banking.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;

import java.util.List;

public record ReconciliationDashboardDTO(
        List<BankTransaction> unmatchedBankLines,
        List<Invoice> unmatchedErpLines,
        List<MatchSuggestionDTO> suggestions) {
}
