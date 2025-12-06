package com.casrusil.SII_ERP_AI.modules.sso.domain.port.in;

import com.casrusil.SII_ERP_AI.modules.sso.domain.model.Company;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

/**
 * Caso de uso para gestionar empresas en el sistema.
 * 
 * <p>
 * Este contrato define las operaciones de gestión del ciclo de vida de
 * empresas,
 * incluyendo registro, actualización y cambios de estado. Es implementado por
 * {@link com.casrusil.SII_ERP_AI.modules.sso.application.service.CompanyManagementService}
 * en la capa de aplicación.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Registrar nuevas empresas en el sistema multi-tenant</li>
 * <li>Actualizar información de empresas existentes</li>
 * <li>Activar/desactivar empresas (soft delete)</li>
 * <li>Validar unicidad de RUT y formato</li>
 * </ul>
 * 
 * <h2>Ejemplo de uso:</h2>
 * 
 * <pre>{@code
 * // Registrar nueva empresa
 * Company company = manageCompanyUseCase.registerCompany(
 *         "76.123.456-7",
 *         "ACME Corp",
 *         "admin@acme.cl");
 * 
 * // Actualizar empresa
 * Company updated = manageCompanyUseCase.updateCompany(
 *         company.getId(),
 *         "ACME Corporation S.A.",
 *         "contact@acme.cl");
 * }</pre>
 * 
 * @see Company
 * @see com.casrusil.SII_ERP_AI.modules.sso.application.service.CompanyManagementService
 * @since 1.0
 */
public interface ManageCompanyUseCase {

    /**
     * Registra una nueva empresa en el sistema.
     * 
     * <p>
     * Crea una nueva empresa con los datos proporcionados y publica un
     * {@link com.casrusil.SII_ERP_AI.modules.sso.domain.event.CompanyCreatedEvent}
     * que dispara la creación automática del plan de cuentas contable.
     * 
     * @param rut         RUT de la empresa en formato chileno (ej: "76.123.456-7")
     * @param razonSocial Razón social de la empresa
     * @param email       Email de contacto de la empresa
     * @return La empresa recién creada
     * @throws IllegalArgumentException si el RUT es inválido o ya existe
     * @see com.casrusil.SII_ERP_AI.modules.sso.domain.event.CompanyCreatedEvent
     */
    Company registerCompany(String rut, String razonSocial, String email);

    /**
     * Actualiza la información de una empresa existente.
     * 
     * @param id          ID de la empresa a actualizar
     * @param razonSocial Nueva razón social
     * @param email       Nuevo email de contacto
     * @return La empresa actualizada
     * @throws IllegalArgumentException si la empresa no existe
     */
    Company updateCompany(CompanyId id, String razonSocial, String email);

    /**
     * Activa una empresa previamente desactivada.
     * 
     * <p>
     * Una empresa activa puede acceder al sistema y realizar operaciones.
     * 
     * @param id ID de la empresa a activar
     * @throws IllegalArgumentException si la empresa no existe
     */
    void activateCompany(CompanyId id);

    /**
     * Desactiva una empresa (soft delete).
     * 
     * <p>
     * Una empresa desactivada no puede acceder al sistema pero sus datos
     * se mantienen para auditoría e histórico.
     * 
     * @param id ID de la empresa a desactivar
     * @throws IllegalArgumentException si la empresa no existe
     */
    void deactivateCompany(CompanyId id);
}
