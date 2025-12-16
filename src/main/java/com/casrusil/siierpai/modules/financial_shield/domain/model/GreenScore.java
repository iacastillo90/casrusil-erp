package com.casrusil.siierpai.modules.financial_shield.domain.model;

import java.util.List;

/**
 * Representa el "Pasaporte Verde" de la empresa.
 * Un puntaje alto desbloquea tasas preferenciales (Crédito Verde).
 */
public record GreenScore(
        int score, // 0-100
        String level, // Ej: "PYME_CARBONO_CONSCIENTE"
        List<String> benefits // Beneficios activos
) {
    public boolean isEligibleForGreenCredit() {
        return score >= 70;
    }
}
