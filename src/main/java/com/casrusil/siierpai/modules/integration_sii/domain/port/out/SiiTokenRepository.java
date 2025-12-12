package com.casrusil.siierpai.modules.integration_sii.domain.port.out;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para persistencia de tokens de autenticación del SII.
 * 
 * <p>
 * Gestiona el almacenamiento y recuperación de tokens de sesión del SII
 * en entornos multi-tenant. Los tokens tienen una validez de 24 horas y deben
 * ser renovados automáticamente.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Almacenar tokens por empresa (multi-tenancy)</li>
 * <li>Recuperar tokens válidos para operaciones SOAP</li>
 * <li>Listar todos los tokens (para scheduler de renovación)</li>
 * <li>Eliminar tokens expirados</li>
 * </ul>
 * 
 * @see SiiToken
 * @see com.casrusil.siierpai.modules.integration_sii.application.service.SiiAuthenticationService
 * @since 1.0
 */
public interface SiiTokenRepository {

    /**
     * Save or update a token for a company.
     */
    void save(CompanyId companyId, SiiToken token);

    /**
     * Find a token by company ID.
     */
    Optional<SiiToken> findByCompanyId(CompanyId companyId);

    /**
     * Get all stored tokens (for refresh scheduler).
     */
    Map<CompanyId, SiiToken> findAll();

    /**
     * Remove a token for a company.
     */
    void delete(CompanyId companyId);
}
