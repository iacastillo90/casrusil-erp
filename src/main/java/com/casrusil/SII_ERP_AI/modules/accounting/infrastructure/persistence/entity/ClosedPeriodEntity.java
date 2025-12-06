package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para periodos contables cerrados.
 * 
 * <p>
 * Registra los meses que han sido cerrados contablemente, impidiendo
 * nuevas modificaciones o asientos con fecha dentro de ese periodo.
 * 
 * <p>
 * Es fundamental para asegurar la inmutabilidad de los estados financieros
 * históricos.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "closed_periods", schema = "accounting", uniqueConstraints = @UniqueConstraint(columnNames = {
        "company_id", "period" }))
public class ClosedPeriodEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 7) // Format: YYYY-MM
    private String period;

    @Column(name = "closed_at", nullable = false)
    private Instant closedAt;

    @Column(name = "closed_by", nullable = false)
    private UUID closedBy;

    @Column(name = "profit_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal profitLoss;

    public ClosedPeriodEntity() {
    }

    public ClosedPeriodEntity(UUID id, UUID companyId, String period, Instant closedAt, UUID closedBy,
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

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public UUID getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(UUID closedBy) {
        this.closedBy = closedBy;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }
}
