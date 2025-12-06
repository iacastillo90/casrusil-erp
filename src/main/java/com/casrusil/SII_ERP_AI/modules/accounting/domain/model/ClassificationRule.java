package com.casrusil.SII_ERP_AI.modules.accounting.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

import java.time.Instant;
import java.util.UUID;

/**
 * Regla de clasificación contable aprendida del comportamiento del usuario.
 * Cuando el usuario corrige un asiento contable, el sistema extrae patrones
 * y crea reglas para aplicar automáticamente en el futuro.
 */
public class ClassificationRule {
    private final UUID id;
    private final CompanyId companyId;
    private final String pattern; // Patrón de descripción o proveedor
    private final String accountCode; // Código de cuenta a aplicar
    private double confidence; // Score de confianza (0.0 - 1.0)
    private final String learnedFrom; // Descripción de dónde se aprendió
    private final Instant createdAt;
    private int timesApplied; // Contador de veces que se ha usado
    private int timesConfirmed; // Contador de veces que fue correcta

    public ClassificationRule(UUID id, CompanyId companyId, String pattern,
            String accountCode, double confidence, String learnedFrom) {
        this.id = id;
        this.companyId = companyId;
        this.pattern = pattern;
        this.accountCode = accountCode;
        this.confidence = confidence;
        this.learnedFrom = learnedFrom;
        this.createdAt = Instant.now();
        this.timesApplied = 0;
        this.timesConfirmed = 0;
    }

    /**
     * Crea una nueva regla de clasificación.
     */
    public static ClassificationRule create(CompanyId companyId, String pattern,
            String accountCode, String learnedFrom) {
        return new ClassificationRule(
                UUID.randomUUID(),
                companyId,
                pattern,
                accountCode,
                0.7, // Confianza inicial moderada
                learnedFrom);
    }

    /**
     * Verifica si el patrón coincide con una descripción dada.
     */
    public boolean matches(String description) {
        if (description == null || pattern == null) {
            return false;
        }

        String normalizedDesc = description.toLowerCase().trim();
        String normalizedPattern = pattern.toLowerCase().trim();

        // Coincidencia exacta o contiene el patrón
        return normalizedDesc.equals(normalizedPattern) ||
                normalizedDesc.contains(normalizedPattern);
    }

    /**
     * Incrementa el contador de aplicaciones y ajusta la confianza.
     */
    public void recordApplication(boolean wasCorrect) {
        timesApplied++;
        if (wasCorrect) {
            timesConfirmed++;
            // Aumentar confianza gradualmente
            confidence = Math.min(1.0, confidence + 0.05);
        } else {
            // Disminuir confianza si fue incorrecta
            confidence = Math.max(0.0, confidence - 0.1);
        }
    }

    /**
     * Verifica si la regla tiene suficiente confianza para ser aplicada
     * automáticamente.
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public String getPattern() {
        return pattern;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getLearnedFrom() {
        return learnedFrom;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getTimesApplied() {
        return timesApplied;
    }

    public int getTimesConfirmed() {
        return timesConfirmed;
    }
}
