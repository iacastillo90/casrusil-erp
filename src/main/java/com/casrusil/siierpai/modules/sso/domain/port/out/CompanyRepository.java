package com.casrusil.siierpai.modules.sso.domain.port.out;

import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para persistencia de empresas.
 * 
 * <p>
 * Define el contrato de persistencia para entidades {@link Company} siguiendo
 * el patrón Repository de DDD. Implementado por
 * {@link com.casrusil.siierpai.modules.sso.infrastructure.adapter.out.persistence.CompanyJpaAdapter}
 * usando JPA.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Persistir y recuperar empresas</li>
 * <li>Validar unicidad de RUT</li>
 * <li>Listar todas las empresas (para scheduler multi-tenant)</li>
 * </ul>
 * 
 * @see Company
 * @see com.casrusil.siierpai.modules.sso.infrastructure.adapter.out.persistence.CompanyJpaAdapter
 * @since 1.0
 */
public interface CompanyRepository {

    /**
     * Persiste una empresa (crear o actualizar).
     * 
     * @param company La empresa a persistir
     * @return La empresa persistida con ID asignado
     */
    Company save(Company company);

    /**
     * Busca una empresa por su ID.
     * 
     * @param id ID de la empresa
     * @return Optional con la empresa si existe, vacío si no
     */
    Optional<Company> findById(CompanyId id);

    /**
     * Busca una empresa por su RUT.
     * 
     * <p>
     * Útil para validar unicidad de RUT durante el registro.
     * 
     * @param rut RUT de la empresa (formato: "76.123.456-7")
     * @return Optional con la empresa si existe, vacío si no
     */
    Optional<Company> findByRut(String rut);

    /**
     * Lista todas las empresas del sistema.
     * 
     * <p>
     * Usado por el
     * {@link com.casrusil.siierpai.modules.ai_assistant.application.scheduler.FinancialAdvisorScheduler}
     * para procesar todas las empresas en modo multi-tenant.
     * 
     * @return Lista de todas las empresas (activas e inactivas)
     */
    List<Company> findAll();
}
