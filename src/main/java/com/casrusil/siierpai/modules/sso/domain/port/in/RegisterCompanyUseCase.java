package com.casrusil.siierpai.modules.sso.domain.port.in;

import com.casrusil.siierpai.modules.sso.domain.model.Company;

/**
 * Puerto de entrada para el caso de uso de registro de empresas.
 * Permite registrar nuevas empresas en el sistema con su información
 * tributaria.
 * 
 * @since 1.0
 */
public interface RegisterCompanyUseCase {
    /**
     * Registra una nueva empresa en el sistema.
     * 
     * @param rut         RUT de la empresa (formato: 12345678-9)
     * @param razonSocial Razón social de la empresa
     * @return La empresa registrada con su ID asignado
     * @throws IllegalArgumentException si el RUT es inválido o la razón social está
     *                                  vacía
     */
    Company registerCompany(String rut, String razonSocial);
}
