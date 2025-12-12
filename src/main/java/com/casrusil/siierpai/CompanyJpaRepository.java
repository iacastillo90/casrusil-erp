package com.casrusil.siierpai;

import com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para empresas.
 * 
 * <p>
 * Permite buscar empresas por RUT o ID, fundamental para el proceso de login
 * y la validación de multi-tenancy.
 * 
 * @since 1.0
 */
@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyEntity, UUID> {
    Optional<CompanyEntity> findByRut(String rut);
}
