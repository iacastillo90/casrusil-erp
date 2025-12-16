package com.casrusil.siierpai.modules.banking.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MatchSuggestionDTO(
        UUID bankTransactionId,
        UUID erpInvoiceId,
        BigDecimal amountDifference,
        long daysDifference,
        String confidenceLevel // HIGH, MEDIUM, LOW
) {
}
