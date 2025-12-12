package com.casrusil.siierpai.modules.integration_sii.domain.port.in;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;

/**
 * Puerto de entrada para el caso de uso de autenticación con el SII.
 * Permite obtener un token de sesión válido para interactuar con los servicios
 * web del SII.
 * 
 * @since 1.0
 */
public interface AuthenticateSiiUseCase {
    /**
     * Autentica al usuario en el SII usando un certificado digital.
     * 
     * @param certificate Certificado digital PKCS#12 de la empresa
     * @return Token de sesión válido para realizar operaciones en el SII
     * @throws com.casrusil.siierpai.shared.domain.exception.DomainException si la
     *                                                                         autenticación
     *                                                                         falla
     */
    SiiToken authenticate(SiiCertificate certificate);
}
