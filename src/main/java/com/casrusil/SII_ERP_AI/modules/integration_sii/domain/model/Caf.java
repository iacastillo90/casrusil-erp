package com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model;

import java.security.PrivateKey;

/**
 * Representa el archivo CAF (Código de Autorización de Folios) del SII.
 * Contiene el rango de folios autorizados y la llave privada para timbrar.
 */
public record Caf(
        String xmlContent, // El XML crudo <AUTORIZACION>...</AUTORIZACION>
        Long rangoDesde,
        Long rangoHasta,
        PrivateKey privateKey, // Llave RSA extraída del CAF para firmar el TED
        String tipoDte // "33", "34", etc.
) {
    public boolean containsFolio(Long folio) {
        return folio >= rangoDesde && folio <= rangoHasta;
    }
}
