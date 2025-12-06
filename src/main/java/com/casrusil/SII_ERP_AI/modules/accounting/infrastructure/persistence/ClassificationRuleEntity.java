package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para reglas de clasificación contable.
 */
/**
 * Entidad JPA para reglas de clasificación automática.
 * 
 * <p>
 * Define patrones (keywords) para asignar automáticamente una cuenta contable
 * a movimientos bancarios o transacciones importadas.
 * 
 * <p>
 * Ejemplo: Si la glosa contiene "LIDER", asignar a cuenta "Supermercado".
 * 
 * @since 1.0
 */
@Entity
@Table(name = "classification_rules")
public class ClassificationRuleEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 500)
    private String pattern;

    @Column(name = "account_code", nullable = false, length = 20)
    private String accountCode;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "learned_from", length = 1000)
    private String learnedFrom;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "times_applied", nullable = false)
    private int timesApplied = 0;

    @Column(name = "times_confirmed", nullable = false)
    private int timesConfirmed = 0;

    // Constructors
    public ClassificationRuleEntity() {
    }

    public ClassificationRuleEntity(UUID id, UUID companyId, String pattern, String accountCode,
            double confidence, String learnedFrom, Instant createdAt,
            int timesApplied, int timesConfirmed) {
        this.id = id;
        this.companyId = companyId;
        this.pattern = pattern;
        this.accountCode = accountCode;
        this.confidence = confidence;
        this.learnedFrom = learnedFrom;
        this.createdAt = createdAt;
        this.timesApplied = timesApplied;
        this.timesConfirmed = timesConfirmed;
    }

    // Getters and Setters
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

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getLearnedFrom() {
        return learnedFrom;
    }

    public void setLearnedFrom(String learnedFrom) {
        this.learnedFrom = learnedFrom;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getTimesApplied() {
        return timesApplied;
    }

    public void setTimesApplied(int timesApplied) {
        this.timesApplied = timesApplied;
    }

    public int getTimesConfirmed() {
        return timesConfirmed;
    }

    public void setTimesConfirmed(int timesConfirmed) {
        this.timesConfirmed = timesConfirmed;
    }
}
