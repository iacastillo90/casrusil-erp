package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Represents a closed accounting period.
 * Once a period is closed, no modifications to accounting entries are allowed
 * for that period.
 */
public class ClosedPeriod {
    private final UUID id;
    private final CompanyId companyId;
    private final YearMonth period;
    private final Instant closedAt;
    private final UserId closedBy;
    private final BigDecimal profitLoss;

    public ClosedPeriod(CompanyId companyId, YearMonth period, UserId closedBy, BigDecimal profitLoss) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.period = period;
        this.closedAt = Instant.now();
        this.closedBy = closedBy;
        this.profitLoss = profitLoss;
    }

    public ClosedPeriod(UUID id, CompanyId companyId, YearMonth period, Instant closedAt, UserId closedBy,
            BigDecimal profitLoss) {
        this.id = id;
        this.companyId = companyId;
        this.period = period;
        this.closedAt = closedAt;
        this.closedBy = closedBy;
        this.profitLoss = profitLoss;
    }

    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public YearMonth getPeriod() {
        return period;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public UserId getClosedBy() {
        return closedBy;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }
}
