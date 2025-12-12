package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.in.ManageCompanyUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.exception.DomainException;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para la gestión administrativa de empresas.
 * 
 * <p>
 * Implementa los casos de uso para crear, actualizar y gestionar el estado
 * (activación/desactivación) de las empresas en el sistema multi-tenant.
 * 
 * <h2>Operaciones:</h2>
 * <ul>
 * <li>Registro de nuevas empresas (validando unicidad de RUT).</li>
 * <li>Actualización de perfil corporativo.</li>
 * <li>Control de ciclo de vida (activar/desactivar acceso).</li>
 * </ul>
 * 
 * @see ManageCompanyUseCase
 * @see Company
 * @since 1.0
 */
@Service
public class CompanyManagementService implements ManageCompanyUseCase {

    private final CompanyRepository companyRepository;

    public CompanyManagementService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public Company registerCompany(String rut, String razonSocial, String email) {
        if (companyRepository.findByRut(rut).isPresent()) {
            throw new DomainException("Company with RUT " + rut + " already exists") {
            };
        }
        Company company = Company.create(rut, razonSocial, email);
        return companyRepository.save(company);
    }

    @Override
    @Transactional
    public Company updateCompany(CompanyId id, String razonSocial, String email) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new DomainException("Company not found") {
                });
        company.updateProfile(razonSocial, email);
        return companyRepository.save(company);
    }

    @Override
    @Transactional
    public void activateCompany(CompanyId id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new DomainException("Company not found") {
                });
        company.activate();
        companyRepository.save(company);
    }

    @Override
    @Transactional
    public void deactivateCompany(CompanyId id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new DomainException("Company not found") {
                });
        company.deactivate();
        companyRepository.save(company);
    }
}
