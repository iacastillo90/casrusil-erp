package com.casrusil.siierpai.modules.sso.application.dto;

import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.User;

/**
 * Resultado de una operación de autenticación en la capa de aplicación.
 * 
 * <p>
 * Encapsula el token generado y las entidades involucradas para que
 * la capa de infraestructura (Controller) pueda construir la respuesta
 * adecuada.
 * 
 * @param token   JWT generado
 * @param user    Usuario autenticado
 * @param company Empresa del usuario
 */
public record AuthResult(
        String token,
        User user,
        Company company) {
}
