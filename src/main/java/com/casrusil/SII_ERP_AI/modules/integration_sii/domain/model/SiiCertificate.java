package com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;

/**
 * Represents a Digital Certificate used for SII authentication and signing.
 */
public record SiiCertificate(
        X509Certificate certificate,
        PrivateKey privateKey,
        String rut,
        Instant validFrom,
        Instant validUntil) {
    public boolean isValid() {
        Instant now = Instant.now();
        return !now.isBefore(validFrom) && !now.isAfter(validUntil);
    }
}
