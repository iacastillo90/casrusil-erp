package com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.repository;

import com.casrusil.SII_ERP_AI.modules.accounting.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para cuentas contables.
 * 
 * <p>
 * Provee métodos CRUD y búsquedas por código y empresa.
 * 
 * @since 1.0
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByCompanyIdAndCode(UUID companyId, String code);

    List<AccountEntity> findByCompanyId(UUID companyId);
}
