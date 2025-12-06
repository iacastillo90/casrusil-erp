package com.casrusil.SII_ERP_AI.modules.integration_sii.domain.model;

import java.time.Instant;

/**
 * Represents an Authentication Token provided by the SII.
 * Tokens typically expire after 1 hour.
 */
public record SiiToken(
        String token,
        Instant expiresAt) {
    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }
}
