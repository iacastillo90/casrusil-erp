package com.casrusil.siierpai.modules.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountMovement(
                UUID entryId,
                LocalDate date,
                String gloss,
                BigDecimal debit,
                BigDecimal credit,
                BigDecimal balance // 💡 Calculated on the fly
) {
        public AccountMovement(UUID entryId, java.time.Instant occurredOn, String gloss, BigDecimal debit,
                        BigDecimal credit, BigDecimal balance) {
                this(entryId, occurredOn.atZone(java.time.ZoneId.systemDefault()).toLocalDate(), gloss, debit, credit,
                                balance);
        }
}
