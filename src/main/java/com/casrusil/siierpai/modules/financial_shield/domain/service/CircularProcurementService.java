package com.casrusil.siierpai.modules.financial_shield.domain.service;

import org.springframework.stereotype.Service;

/**
 * Motor de Economía Circular (El "Eslabón Perdido").
 * Sugiere proveedores alternativos "verdes" dentro de la red.
 */
@Service
public class CircularProcurementService {

    public String suggestGreenAlternatives(String category) {
        // Lógica simulada para MVP (Idealmente buscaría en una base de datos de
        // proveedores certificados)
        if ("COMBUSTIBLE".equalsIgnoreCase(category))
            return "Energía Solar SpA (Partner Verde)";
        if ("PAPELERIA".equalsIgnoreCase(category))
            return "EcoPapel Reciclado Ltda";
        if ("TRANSPORTE".equalsIgnoreCase(category))
            return "Logística Eléctrica Chile";
        return null;
    }
}
