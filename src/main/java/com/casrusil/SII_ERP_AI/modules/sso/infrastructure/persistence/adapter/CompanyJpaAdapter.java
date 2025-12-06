package com.casrusil.SII_ERP_AI.modules.sso.infrastructure.persistence.adapter;

import com.casrusil.SII_ERP_AI.modules.sso.domain.model.Company;
import com.casrusil.SII_ERP_AI.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.SII_ERP_AI.modules.sso.infrastructure.persistence.entity.CompanyEntity;
import com.casrusil.SII_ERP_AI.modules.sso.infrastructure.persistence.repository.CompanyJpaRepository;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para empresas.
 * 
 * <p>
 * Implementa {@link CompanyRepository} para la gestión de datos de empresas
 * (tenants).
 * 
 * @since 1.0
 */
@Component
public class CompanyJpaAdapter implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;

    public CompanyJpaAdapter(CompanyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Company save(Company company) {
        CompanyEntity entity = toEntity(company);
        CompanyEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Company> findById(CompanyId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<Company> findByRut(String rut) {
        return jpaRepository.findByRut(rut).map(this::toDomain);
    }

    @Override
    public List<Company> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CompanyEntity toEntity(Company company) {
        return new CompanyEntity(
                company.getId().getValue(),
                company.getRut(),
                company.getRazonSocial(),
                company.getEmail(),
                company.isActive(),
                company.getCreatedAt());
    }

    private Company toDomain(CompanyEntity entity) {
        return new Company(
                new CompanyId(entity.getId()),
                entity.getRut(),
                entity.getRazonSocial(),
                entity.getEmail(),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
