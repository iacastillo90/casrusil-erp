package com.casrusil.siierpai.modules.financial_shield.domain.service;

import com.casrusil.siierpai.modules.financial_shield.domain.model.GreenScore;
import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository.SustainabilityRecordRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcula el "Score de Crédito Verde" basado en datos reales del SII.
 * Esta es la pieza clave para la postulación al Clúster 6.
 */
@Service
public class GreenCreditScoringService {

    private final SustainabilityRecordRepository sustainabilityRepository;

    public GreenCreditScoringService(SustainabilityRecordRepository sustainabilityRepository) {
        this.sustainabilityRepository = sustainabilityRepository;
    }

    public GreenScore calculateScore(CompanyId companyId) {
        // 1. Obtener huella de carbono de los últimos 6 meses (Dato Real)
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalFootprint = sustainabilityRepository.sumCarbonFootprintBetween(now.minusMonths(6), now);

        if (totalFootprint == null)
            totalFootprint = BigDecimal.ZERO;

        // 2. Algoritmo de Scoring (Heurística MVP para Demo)
        // Menos emisiones = Mayor puntaje
        int baseScore = 60;

        // Si la huella es baja (ej: < 500kg en 6 meses), bonificar
        if (totalFootprint.doubleValue() < 500.0) {
            baseScore += 25;
        }

        // TODO: En el futuro, cruzar con facturas de "Energía Renovable" para sumar más
        // puntos

        List<String> benefits = new ArrayList<>();
        String level = "EN_TRANSICION";

        if (baseScore >= 70) {
            level = "PYME_SOSTENIBLE";
            benefits.add("Acceso a Crédito Verde CORFO");
            benefits.add("Tasa Preferencial (0.6%)");
        } else {
            benefits.add("Acceso a Factoring Estándar");
        }

        return new GreenScore(baseScore, level, benefits);
    }
}
